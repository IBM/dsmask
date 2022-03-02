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

import java.io.File;
import java.io.FileInputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import com.ibm.dsmask.jconf.impl.*;
import com.ibm.dsmask.jconf.beans.*;
import com.ibm.dsmask.util.PasswordVault;
import com.ibm.dsmask.util.DsMaskVersion;

/**
 * dsmask-jconf driver program (entry point).
 * @author zinal
 */
public class BuildConf implements Runnable {

    private static final org.slf4j.Logger LOG = Utils.logger(BuildConf.class);

    // property keys
    public static final String CONF_TAB_TYPE = "in.tab.type";
    public static final String CONF_TAB_FNAME = "in.tab.file.name";
    public static final String CONF_IGC_URL = "in.tab.igc.url";
    public static final String CONF_IGC_VAULT = "in.tab.igc.vault";
    public static final String CONF_IGC_USER = "in.tab.igc.username";
    public static final String CONF_IGC_PASS = "in.tab.igc.password";
    public static final String CONF_DC_RULES = "in.dc.rules";
    public static final String CONF_MASK_RULES = "in.masking.rules";
    public static final String CONF_MASK_CTX = "in.masking.context";
    public static final String CONF_OUT_DIR = "out.dir";
    public static final String CONF_OUT_CONF = "out.config";
    public static final String CONF_OUT_DUMP = "out.dump";
    // property constant values
    public static final String VAL_TT_FILE = "file";
    public static final String VAL_TT_IGC = "igc";

    private final Properties props;

    private List<TableInfo> tables = null;
    private DataClassRegistry dcReg = null;
    private MaskingRuleRegistry ruleReg = null;
    private RuleSelector ruleSelector = null;

    public BuildConf(Properties props) {
        this.props = props;
    }

    public static void main(String[] args) {
        try {
            if (args.length != 1) {
                System.out.println("USAGE: " + BuildConf.class.getName()
                        + " jobfile.xml");
                System.exit(1);
            }
            final Properties props = new Properties();
            try (FileInputStream fis = new FileInputStream(args[0])) {
                props.loadFromXML(fis);
            }

            new BuildConf(props).runImpl();

        } catch(Exception ex) {
            LOG.error("FATAL: operation failed", ex);
            System.exit(1);
        }
    }

    @Override
    public void run() {
        try {
            runImpl();
        } catch(Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private String getConfig(String name) {
        String v = props.getProperty(name);
        if (v==null) {
            throw new RuntimeException("Missing property " + name + " in the job file");
        }
        return v;
    }

    private void runImpl() throws Exception {
        LOG.info("DsMask {} BuildConf", DsMaskVersion.VERSION);
        // Loading the table information and data classes.
        readTablesAndClasses();
        // load masking rules
        ruleReg = buildRules();
        LOG.info("Masking rules loaded, total {} rules, "
                + "{} functions, {} keys.", ruleReg.getRulesCount(),
                ruleReg.getFunctionCount(), ruleReg.getKeyCount());
        // Validate scripts
        new ScriptChecker(ruleReg) . check();
        // create and configure rule selector
        ruleSelector = new RuleSelector();
        ruleSelector.setDataClassRegistry(dcReg);
        ruleSelector.setRuleRegistry(ruleReg);
        // retrieve the name of the masking context
        final String context = Utils.lower(props.getProperty(CONF_MASK_CTX));
        LOG.info("Masking context: [{}].", context);
        // re-create empty configuration database
        final String configName =
                Utils.safeConfigName(props.getProperty(CONF_OUT_CONF));
        final String configDir = props.getProperty(CONF_OUT_DIR);
        final String dbPath = Utils.makeConfigDbPath(configDir, configName);
        DbManager.deleteFiles(dbPath);
        try (DbManager db = new DbManager()) {
            db.create(dbPath);
            LOG.info("Configuration database opened.");
            // save all masking keys
            db.saveKeys(ruleReg.retrieveKeys());
            LOG.info("Keys saved.");
            // for each table, build masking profile and store it
            //    in the configuration database
            for ( TableInfo ti : tables ) {
                MaskingProfile mp = ruleSelector.select(ti, context);
                if (mp.hasOperations()) {
                    db.save(mp);
                    LOG.info("Table {}.{} processed, "
                            + "total {} masking operations", ti.getDatabase(),
                            ti.getName(), mp.getOperations().size());
                    if (LOG.isInfoEnabled()) {
                        printOperations(mp);
                    }

                } else {
                    LOG.info("Table {}.{} skipped - no matching rules",
                            ti.getDatabase(), ti.getName());
                }
            }
        }
        LOG.info("DsMask configurator complete, database closed.");
        dumpConfig();
    }

    private void readTablesAndClasses() throws Exception {
        // Determine the data source (files and/or IGC)
        boolean useFiles = false;
        boolean useIGC = false;
        final String tabType = Utils.lower(props.getProperty(CONF_TAB_TYPE));
        if (tabType.length() == 0) {
            useFiles = true;
        } else {
            for (String srctype : tabType.split("[,:]")) {
                if (srctype.length()==0 || VAL_TT_FILE.equalsIgnoreCase(srctype)) {
                    useFiles = true;
                } else if (VAL_TT_IGC.equalsIgnoreCase(srctype)) {
                    useIGC = true;
                } else {
                    throw new Exception("Unsupported table info source ["
                            + srctype + "]");
                }
            }
        }

        final Map<String, TableInfo> tableInfo = new TreeMap<>();
        final DataClassRules currentRules = new DataClassRules();

        if (useFiles) {
            String pathname = props.getProperty(CONF_TAB_FNAME);
            if (pathname != null) {
                for ( TableInfo ti : new TableInfoXmlReader().readList(pathname) ) {
                    tableInfo.put(ti.getFullName().toLowerCase(), ti);
                }
            }
            pathname = props.getProperty(CONF_DC_RULES);
            if (pathname != null) {
                new DataClassRulesXmlReader().read(currentRules, pathname);
            }
        }

        if (useIGC) {
            final String url = getConfig(CONF_IGC_URL);
            final PasswordVault.Entry e = PasswordVault.readProps
                (props, CONF_IGC_VAULT, CONF_IGC_USER, CONF_IGC_PASS);
            try (MetadataIgcReader reader
                    = new MetadataIgcReader(url, e.login, e.password)) {
                for ( TableInfo ti :  reader.readTables() ) {
                    tableInfo.put(ti.getFullName().toLowerCase(), ti);
                }
                reader.readDataClassRules(currentRules);
            }
        }

        this.tables = new ArrayList<>(tableInfo.values());
        LOG.info("Table information loaded, total {} tables.", tables.size());

        currentRules.prepare();
        this.dcReg = new DataClassRegistry(currentRules.collect(this.tables));
        LOG.info("Data classes loaded, total {} classes.", this.dcReg.size());
    }

    private MaskingRuleRegistry buildRules() throws Exception {
        final String pathname = props.getProperty(CONF_MASK_RULES);
        final MaskingRulesXmlReader reader = new MaskingRulesXmlReader();
        if (new File(pathname).isDirectory())
            return reader.readBulk(pathname);
        return reader.read(pathname);
    }

    private void printOperations(MaskingProfile mp) {
        for (MaskingOperation mo : mp.getOperations()) {
            LOG.info("\tOperation: rule {}", mo.getMaskingRule().getName());
            for (FieldInfo fi : mo.getArguments()) {
                LOG.info("\t\tINPUT\t{}", fi.getName());
            }
            for (FieldInfo fi : mo.getOutputs()) {
                LOG.info("\t\tOUTPUT\t{}", fi.getName());
            }
        }
    }

    private void dumpConfig() throws Exception {
        String dumpName = props.getProperty(CONF_OUT_DUMP);
        if (dumpName==null || dumpName.trim().length()==0)
            return;
        if (tables!=null) {
            String pathName = dumpName + ".tables.xml";
            new TableInfoXmlWriter().write(pathName, tables);
            LOG.info("Table structure dump has been written to {}", pathName);
        }
        if (dcReg!=null) {
            String pathName = dumpName + ".dcs.txt";
            try (PrintWriter pw = new PrintWriter(pathName, "UTF-8")) {
                for (DataClass dc : dcReg.collect()) {
                    if (dc.isConfidential())
                        pw.println(dc.getName());
                }
            }
            LOG.info("Confidential data classes have been written to {}", pathName);
        }
    }

}
