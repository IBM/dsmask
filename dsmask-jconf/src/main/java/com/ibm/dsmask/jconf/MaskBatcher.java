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
import java.util.List;
import java.util.TreeSet;
import java.util.HashSet;
import java.util.Set;
import com.ibm.dsmask.jconf.beans.*;
import com.ibm.dsmask.jconf.impl.JobManager;
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
    public static final String CONF_JOB_CMD = "job.cmd";
    public static final String CONF_JOB_PROJ = "job.project";
    public static final String CONF_JOB_NAME = "job.name";
    public static final String CONF_DB_LOGICAL = "dbname.logical";
    public static final String CONF_DB_SOURCE = "dbname.source";
    public static final String CONF_DB_TARGET = "dbname.target";

    private final Mode mode;
    private final Properties props;
    private final String tableSetName;

    private MetadataIgcReader igcReader = null;
    private TableSetManager tsManager = null;

    private String confJobCmd;
    private String confJobName;
    private String confJobProject;
    private String confDbLogical;
    private String confDbSource;
    private String confDbTarget;

    public MaskBatcher(Mode mode, Properties props, String tableSetName) {
        this.mode = mode;
        this.props = props;
        this.tableSetName = tableSetName;
    }

    public static void main(String[] args) {
        try {
            if (args.length != 3) {
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

            try (MaskBatcher mb = new MaskBatcher(mode, props, args[2])) {
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
                + " REFRESH jobfile.xml tableSet");
        System.out.println("\t\t" + MaskBatcher.class.getName()
                + " RUN     jobfile.xml tableSet");
        System.out.println("\t\t" + MaskBatcher.class.getName()
                + " STATUS  jobfile.xml { tableSet | - }");
        System.out.println("\t\t" + MaskBatcher.class.getName()
                + " KILL    jobfile.xml { tableSet | - }");
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
            LOG.info("Starting operation {} on tableSet '{}'...",
                    mode, tableSetName);
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
            LOG.info("Operation complete.");
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private String configJobCmd() {
        if (confJobCmd==null)
            confJobCmd = getConfig(CONF_JOB_CMD);
        return confJobCmd;
    }

    private String configJobName() {
        if (confJobName==null)
            confJobName = getConfig(CONF_JOB_NAME);
        return confJobName;
    }

    private String configJobProject() {
        if (confJobProject==null)
            confJobProject = getConfig(CONF_JOB_PROJ);
        return confJobProject;
    }

    private String configDbLogical() {
        if (confDbLogical==null)
            confDbLogical = getConfig(CONF_DB_LOGICAL);
        return confDbLogical;
    }

    private String configDbSource() {
        if (confDbSource==null)
            confDbSource = getConfig(CONF_DB_SOURCE);
        return confDbSource;
    }

    private String configDbTarget() {
        if (confDbTarget==null)
            confDbTarget = getConfig(CONF_DB_TARGET);
        return confDbTarget;
    }

    /**
     * STATUS subcommand
     * @throws Exception
     */
    private void runStatus() throws Exception {
        final JobManager jm = new JobManager(
                configJobProject(),
                configJobName(),
                configJobCmd()
        );

        final List<JobInfo> jobs = jm.listJobs();
        LOG.info("Total active jobs found: {}", jobs.size());

        if (tableSetName==null || tableSetName.length()==0
                || "-".equalsIgnoreCase(tableSetName)) {
            // Unfiltered output of running and queued jobs.
            for (JobInfo ji : jobs) {
                LOG.info("\t{}\t{}\t{}",
                        ji.getJobState(),
                        ji.getStartTime(),
                        ji.getJobId());
            }
            return;
        }

        // We need a table set to filter the output.
        List<TableName> tables = grabTsManager().readTableSet(tableSetName);
        // So that we can generate possible job IDs.
        Set<String> jobIds = makeJobIds(tables);
        // Filtered output of running and queued jobs.
        for (JobInfo ji : jobs) {
            if (jobIds.contains(ji.getJobId())) {
                LOG.info("\t{}\t{}\t{}",
                        ji.getJobState(),
                        ji.getStartTime(),
                        ji.getJobId());
            }
        }
    }

    private Set<String> makeJobIds(List<TableName> tables) {
        final Set<String> jobIds = new HashSet<String>();
        for (TableName tn : tables) {
            tn.setDatabase(configDbSource());
            jobIds.add(makeJobId(tn));
        }
        return jobIds;
    }

    private String makeJobId(TableName tn) {
        return configJobName() + "." + safeInvocation(tn.getFullName());
    }

    private static String safeInvocation(String table) {
        return table.replace('.', '-');
    }

    /**
     * RUN subcommand
     * @throws Exception
     */
    private void runRun() throws Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * KILL subcommand
     * @throws Exception
     */
    private void runKill() throws Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * REFRESH subcommand
     * @throws Exception
     */
    private void runRefresh() throws Exception {
        final String dbName = getConfig(CONF_DB_LOGICAL);
        List<TableName> allTables = new ArrayList<>();
        LOG.info("Reading the list of tables for database {}...", dbName);
        final List<TableName> curTables = grabIgcReader().listMaskedTables(dbName);
        LOG.info("\tfound {} confidential tables", curTables.size());
        allTables.addAll(curTables);
        allTables = removeDuplicates(allTables);
        LOG.info("Number of tables after dedup: {}", allTables.size());
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
