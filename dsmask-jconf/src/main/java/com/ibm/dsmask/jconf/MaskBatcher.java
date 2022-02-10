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
import com.ibm.dsmask.jconf.beans.*;
import java.util.Arrays;

/**
 * Data masking batch job executor (entry point).
 * @author zinal
 */
public class MaskBatcher implements Runnable {

    private static final org.slf4j.Logger LOG = Utils.logger(MaskBatcher.class);

    // property keys
    public static final String CONF_TABSET_DIR = "tableSet.dir";
    public static final String CONF_XMETA_URL = "xmeta.url";
    public static final String CONF_XMETA_USER = "xmeta.username";
    public static final String CONF_XMETA_PASS = "xmeta.password";
    public static final String CONF_DSJOB_EXEC = "dsjob.exec";
    public static final String CONF_DSJOB_PATT_RESET = "dsjob.patt.reset";
    public static final String CONF_DSJOB_PATT_RUN = "dsjob.patt.run";

    private final Mode mode;
    private final Properties props;
    private final String tableSet;
    private final String[] dbNames;

    public MaskBatcher(Mode mode, Properties props, String tableSet,
            String[] dbNames) {
        this.mode = mode;
        this.props = props;
        this.tableSet = tableSet;
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

            new MaskBatcher(mode, props, tableSet, dbNames).runImpl();

        } catch(Exception ex) {
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
    public void run() {
        try {
            runImpl();
        } catch(Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private void runImpl() throws Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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
