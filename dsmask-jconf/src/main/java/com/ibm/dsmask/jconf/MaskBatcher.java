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
import java.util.Properties;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import java.util.HashSet;
import java.util.Set;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.apache.commons.text.StringSubstitutor;
import com.ibm.dsmask.jconf.beans.*;
import com.ibm.dsmask.jconf.impl.*;
import com.ibm.dsmask.util.PasswordVault;

/**
 * Data masking batch job executor (entry point).
 * @author zinal
 */
public class MaskBatcher implements Runnable, AutoCloseable, JobConfiguration {

    private static final org.slf4j.Logger LOG = Utils.logger(MaskBatcher.class);

    // property keys for the job
    public static final String JOB_CONF_FILE = "config.file";
    public static final String JOB_PROJ = "job.project";
    public static final String JOB_NAME = "job.name";
    public static final String JOB_GLOBALS = "globals.name";
    public static final String JOB_DB_LOGICAL = "dbname.logical";
    public static final String JOB_DB_SOURCE = "dbname.source";
    public static final String JOB_DB_TARGET = "dbname.target";
    public static final String JOB_TAB_SOURCE = "tabname.source";
    public static final String JOB_TAB_TARGET = "tabname.target";
    public static final String JOB_TAB_PROFILE = "tabname.profile";
    // property keys for configuration
    public static final String CONF_TABSET_DIR = "tableSet.dir";
    public static final String CONF_XMETA_URL = "xmeta.url";
    public static final String CONF_XMETA_USER = "xmeta.username";
    public static final String CONF_XMETA_PASS = "xmeta.password";
    public static final String CONF_XMETA_VAULT = "xmeta.vault";

    private final Mode mode;
    private final Properties propsJob;
    private final Properties propsConfig;
    private final String tableSetName;

    private MetadataIgcReader igcReader = null;
    private TableSetManager tsManager = null;

    private String confJobName;
    private String confJobProject;
    private String confDbLogical;
    private String confDbSource;
    private String confDbTarget;
    private String confTabSource;
    private String confTabTarget;
    private String confTabProfile;

    public MaskBatcher(Mode mode, File jobFile, String tableSetName) {
        this.mode = mode;
        this.propsJob = new Properties();
        loadProperties(propsJob, jobFile);
        this.propsConfig = new Properties();
        loadProperties(propsConfig, new File(getJobOption(propsJob, JOB_CONF_FILE)));
        this.tableSetName = tableSetName;
    }

    private static void loadProperties(Properties props, File file) {
        try (FileInputStream fis = new FileInputStream(file)) {
            props.loadFromXML(fis);
        } catch(Exception ex) {
            throw new RuntimeException("Failed to read property file " + file, ex);
        }
    }

    private static String getJobOption(Properties props, String name) {
        String v = props.getProperty(name);
        if (v==null) {
            throw new RuntimeException("Missing property " + name + " in the job file");
        }
        return v;
    }

    @Override
    public String getOption(String name) {
        String v = propsJob.getProperty(name);
        if (v==null) {
            v = propsConfig.getProperty(name);
        }
        if (v==null) {
            throw new RuntimeException("Missing property [" + name + "] in the configuration");
        }
        return v.trim();
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

            try (MaskBatcher mb = new MaskBatcher(mode, new File(args[1]), args[2])) {
                mb.run();
            }
            LOG.info("Exiting normally.");

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
                + " STOP    jobfile.xml { tableSet | - }");
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
            LOG.info("Starting operation {} on tableSet '{}'",
                    mode, tableSetName);
            switch (mode) {
                case STATUS:
                    runStatus();
                    break;
                case RUN:
                    runRun();
                    break;
                case STOP:
                    runStop();
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

    private String configJobName() {
        if (confJobName==null)
            confJobName = getOption(JOB_NAME);
        return confJobName;
    }

    private String configJobProject() {
        if (confJobProject==null)
            confJobProject = getOption(JOB_PROJ);
        return confJobProject;
    }

    private String configDbLogical() {
        if (confDbLogical==null)
            confDbLogical = getOption(JOB_DB_LOGICAL);
        return confDbLogical;
    }

    private String configDbSource() {
        if (confDbSource==null) {
            confDbSource = propsJob.getProperty(JOB_DB_SOURCE);
            if (confDbSource==null)
                confDbSource = configDbLogical();
        }
        return confDbSource;
    }

    private String configDbTarget() {
        if (confDbTarget==null) {
            confDbTarget = propsJob.getProperty(JOB_DB_TARGET);
            if (confDbTarget==null)
                confDbTarget = configDbSource();
        }
        return confDbTarget;
    }

    private String configTabSource() {
        if (confTabSource==null) {
            confTabSource = getOption(JOB_TAB_SOURCE);
        }
        return confTabSource;
    }

    private String configTabTarget() {
        if (confTabTarget==null) {
            confTabTarget = getOption(JOB_TAB_TARGET);
        }
        return confTabTarget;
    }

    private String configTabProfile() {
        if (confTabProfile==null) {
            confTabProfile = getOption(JOB_TAB_PROFILE);
        }
        return confTabProfile;
    }

    /**
     * STATUS subcommand
     * @throws Exception
     */
    private void runStatus() throws Exception {
        final JobManager jm = new JobManager(
                configJobProject(),
                configJobName(),
                this
        );

        List<JobInfo> jobs = jm.listJobs();
        LOG.info("Total active jobs found: {}", jobs.size());

        jobs = filterJobs(jobs);
        LOG.info("Filtered active jobs: {}", jobs.size());

        for (JobInfo ji : jobs) {
            LOG.info("\t{}\t{}\t{}",
                    ji.getJobState(),
                    ji.getStartTime(),
                    ji.getJobId());
        }
    }

    private List<JobInfo> filterJobs(List<JobInfo> jobs) throws Exception {
        if (tableSetName==null || tableSetName.length()==0
                || "-".equalsIgnoreCase(tableSetName)) {
            return jobs;
        }
        // We need a table set to filter the output.
        List<TableName> tables = grabTsManager().readTableSet(tableSetName);
        return filterJobs(jobs, tables);
    }

    private List<JobInfo> filterJobs(List<JobInfo> jobs, List<TableName> tables)
            throws Exception {
        // So that we can generate possible job IDs.
        Set<String> jobIds = makeJobIds(tables);
        // Filtered output of jobs.
        final List<JobInfo> retval = new ArrayList<>();
        for (JobInfo ji : jobs) {
            if (jobIds.contains(ji.getJobId())) {
                retval.add(ji);
            }
        }
        return retval;
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
        return configJobName() + "." + JobManager.safeInvocation(tn.getFullName());
    }

    /**
     * RUN subcommand
     * @throws Exception
     */
    private void runRun() throws Exception {
        // The job manager
        final JobManager jm = new JobManager(
                configJobProject(),
                configJobName(),
                this
        );
        // The tables to be masked
        List<TableName> tables = grabTsManager().readTableSet(tableSetName);
        if (tables.isEmpty()) {
            LOG.info("Table set {} is empty, nothing to run.", tableSetName);
            return;
        }
        // Checking if those tables are not being processed already.
        int countRunningJobs = 0;
        for (JobInfo ji : filterJobs(jm.listJobs(), tables)) {
            LOG.info("Found job: {}\t{}\t{}",
                    ji.getJobState(),
                    ji.getStartTime(),
                    ji.getJobId());
            ++countRunningJobs;
        }
        if (countRunningJobs > 0) {
            LOG.error("Job startup denied, having {} jobs already running",
                    countRunningJobs);
            return;
        }

        // TODO: job startup should be synchronized, e.g. we should lock
        // concurrent job startup attempts for tables we want to process below.

        final String batchId = UUID.randomUUID().toString();
        LOG.info("Starting new masking jobs with batch ID {}...", batchId);

        // Set repeatable parameters for job startup.
        jm.setBatchId(batchId);
        jm.setGlobalsId(getOption(JOB_GLOBALS));
        jm.setInputDb(configDbSource());
        jm.setOutputDb(configDbTarget());

        Map<String,String> subst = new HashMap<>();
        subst.put("dbname_logical", configDbLogical());
        subst.put("dbname_source", configDbSource());
        subst.put("dbname_target", configDbTarget());

        // Start new masking job for each table.
        for (TableName tn : tables) {
            subst.put("schema", tn.getSchema());
            subst.put("table", tn.getTable());
            final StringSubstitutor ss = new StringSubstitutor(subst);
            String jobId = jm.startJob(
                    ss.replace(configTabSource()),
                    ss.replace(configTabTarget()),
                    ss.replace(configTabProfile())
            );
            LOG.info("\tStarted job {}", jobId);
        }
    }

    /**
     * STOP subcommand
     * @throws Exception
     */
    private void runStop() throws Exception {
        // The job manager
        final JobManager jm = new JobManager(
                configJobProject(),
                configJobName(),
                this
        );
        int countStoppedJobs = 0;
        for (JobInfo ji : filterJobs(jm.listJobs())) {
            LOG.info("Found job: {}\t{}\t{}",
                    ji.getJobState(),
                    ji.getStartTime(),
                    ji.getJobId());
            ++countStoppedJobs;
            try {
                jm.stopJob(ji.getJobId());
                LOG.info("\tJob stopped!");
                ++countStoppedJobs;
            } catch(Exception ex) {
                LOG.error("Failed to stop job", ex);
            }
        }
        LOG.info("Total jobs stopped: {}", countStoppedJobs);
    }

    /**
     * REFRESH subcommand
     * @throws Exception
     */
    private void runRefresh() throws Exception {
        final String dbName = getOption(JOB_DB_LOGICAL);
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

    private MetadataIgcReader grabIgcReader() throws Exception {
        if (igcReader!=null)
            return igcReader;
        final String jdbcUrl = getOption(CONF_XMETA_URL);
        final String username;
        final String password;
        final String vaultKey = propsConfig.getProperty(CONF_XMETA_VAULT);
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
            username = getOption(CONF_XMETA_USER);
            password = getOption(CONF_XMETA_PASS);
        }
        igcReader = new MetadataIgcReader(jdbcUrl, username, password);
        return igcReader;
    }

    private TableSetManager grabTsManager() {
        if (tsManager!=null)
            return tsManager;
        tsManager = new TableSetManager(getOption(CONF_TABSET_DIR));
        return tsManager;
    }

    public static enum Mode {
        STATUS,
        RUN,
        STOP,
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
