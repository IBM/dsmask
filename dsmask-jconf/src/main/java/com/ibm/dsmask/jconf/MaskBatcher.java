/*
 * Copyright (c) IBM Corp. 2018, 2022.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *  Maksim Zinal (IBM) - Initial implementation
 */
package com.ibm.dsmask.jconf;

import java.io.FileInputStream;
import java.util.Properties;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;
import com.ibm.dsmask.jconf.beans.*;
import com.ibm.dsmask.jconf.impl.MetadataIgcReader;
import com.ibm.dsmask.jconf.impl.TableSetManager;
import com.ibm.dsmask.util.PasswordVault;

/**
 * Data masking batch job executor (entry point).
 * @author zinal
 */
public class MaskBatcher implements Runnable, AutoCloseable {

    private static final org.slf4j.Logger LOG = Utils.logger(MaskBatcher.class);

    // property keys
    public static final String CONF_TABSET_DIR = "tableSet.dir";
    public static final String CONF_XMETA_URL = "xmeta.url";
    public static final String CONF_XMETA_USER = "xmeta.username";
    public static final String CONF_XMETA_PASS = "xmeta.password";
    public static final String CONF_XMETA_VAULT = "xmeta.vault";
    public static final String CONF_DSJOB_EXEC = "dsjob.exec";
    public static final String CONF_DSJOB_PATT_RESET = "dsjob.patt.reset";
    public static final String CONF_DSJOB_PATT_RUN = "dsjob.patt.run";

    private final Mode mode;
    private final Properties props;
    private final String tableSetName;
    private final String[] dbNames;

    private MetadataIgcReader igcReader = null;
    private TableSetManager tsManager = null;

    public MaskBatcher(Mode mode, Properties props, String tableSetName,
            String[] dbNames) {
        this.mode = mode;
        this.props = props;
        this.tableSetName = tableSetName;
        this.dbNames = (dbNames==null) ? new String[]{} : dbNames;
    }

    public MaskBatcher(Mode mode, Properties props, String tableSet) {
        this(mode, props, tableSet, null);
    }

    public static void main(String[] args) {
        try {
            if (args.length < 3) {
                usageAndDie();
            }

            final Mode mode = Mode.getMode(args[0]);
            if (mode==null) {
                usageAndDie();
            }

            final Properties props = new Properties();
            try (FileInputStream fis = new FileInputStream(args[1])) {
                props.loadFromXML(fis);
            }

            final String tableSet = args[2];

            final String[] dbNames = 
                    (args.length > 3) ?
                    Arrays.copyOfRange(args, 3, args.length) :
                    null;

            try (MaskBatcher mb = new MaskBatcher(mode, props, tableSet, dbNames)) {
                mb.run();
            }

        } catch(Throwable ex) {
            LOG.error("FATAL: operation failed", ex);
            System.exit(1);
        }
    }
    
    private static void usageAndDie() {
        System.out.println("USAGE:");
        System.out.println("\t\t" + MaskBatcher.class.getName()
                + " STATUS  jobfile.xml tableSet");
        System.out.println("\t\t" + MaskBatcher.class.getName()
                + " RUN     jobfile.xml tableSet");
        System.out.println("\t\t" + MaskBatcher.class.getName()
                + " KILL    jobfile.xml tableSet");
        System.out.println("\t\t" + MaskBatcher.class.getName()
                + " REFRESH jobfile.xml tableSet [dbName ...]");
        System.exit(1);
    }

    @Override
    public void close() throws Exception {
        if (igcReader!=null) {
            igcReader.close();
            igcReader = null;
        }
        if (tsManager!=null) {
            tsManager = null;
        }
    }

    @Override
    public void run() {
        try {
            switch (mode) {
                case STATUS:
                    runStatus();
                     break;
                case RUN:
                    runRun();
                    break;
                case KILL:
                    runKill();
                    break;
                case REFRESH:
                    runRefresh();
                    break;
            }
        } catch(Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private void runStatus() throws Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    private void runRun() throws Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    private void runKill() throws Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private void runRefresh() throws Exception {
        List<TableName> allTables = new ArrayList<>();
        for (String dbName : dbNames) {
            LOG.info("Reading the list of tables for database {}...", dbName);
            final List<TableName> curTables = grabIgcReader().listMaskedTables(dbName);
            LOG.info("\tfound {} confidential tables", curTables.size());
            allTables.addAll(curTables);
        }
        LOG.info("Number of tables before dedup: {}", allTables.size());
        allTables = removeDuplicates(allTables);
        LOG.info("Number of tables before dedup: {}", allTables.size());
        grabTsManager().writeTableSet(tableSetName, allTables);
        LOG.info("Table list written to tableSet {}", tableSetName);
    }

    /**
     * Remove the duplicates from the list of table names.
     * @param input Input list
     * @return Output list without duplicates, order is not preserved
     */
    private List<TableName> removeDuplicates(List<TableName> input) {
        final List<TableName> retval = new ArrayList<>();
        TableName prev = null;
        for ( TableName tn : new TreeSet<TableName>(input) ) {
            if ( prev == null || tn.compareTo(prev)!=0 ) {
                prev = tn;
                retval.add(tn);
            }
        }
        return retval;
    }

    private String getConfig(String name) {
        String v = props.getProperty(name);
        if (v==null) {
            throw new RuntimeException("Missing property " + name + " in the job file");
        }
        return v;
    }

    private MetadataIgcReader grabIgcReader() throws Exception {
        if (igcReader!=null)
            return igcReader;
        final String jdbcUrl = getConfig(CONF_XMETA_URL);
        final String username;
        final String password;
        final String vaultKey = props.getProperty(CONF_XMETA_VAULT);
        if (vaultKey!=null && vaultKey.length() > 0) {
            final PasswordVault.Entry e = new PasswordVault().getEntry(vaultKey);
            if (e == null) {
                throw new RuntimeException("Missing password vault "
                        + "entry for key " + vaultKey
                        + ", please check property " + CONF_XMETA_VAULT
                        + " in the job file");
            }
            username = e.login;
            password = e.password;
        } else {
            username = getConfig(CONF_XMETA_USER);
            password = getConfig(CONF_XMETA_PASS);
        }
        igcReader = new MetadataIgcReader(jdbcUrl, username, password);
        return igcReader;
    }

    private TableSetManager grabTsManager() {
        if (tsManager!=null)
            return tsManager;
        tsManager = new TableSetManager(getConfig(CONF_TABSET_DIR));
        return tsManager;
    }

    public static enum Mode {
        STATUS,
        RUN,
        KILL,
        REFRESH;

        public static Mode getMode(String mode) {
            if (mode==null)
                return null;
            for (Mode m : Mode.values()) {
                if (mode.equalsIgnoreCase(m.name()))
                    return m;
            }
            return null;
        }
    }

}
