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
package com.ibm.dsmask.jconf.impl;

import java.util.List;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import org.apache.commons.text.StringSubstitutor;
import org.apache.commons.text.StringTokenizer;
import com.ibm.dsmask.jconf.beans.JobInfo;
import com.ibm.dsmask.jconf.beans.JobDef;

/**
 * Masking job manager, part of MaskBatcher tool implementation.
 * @author zinal
 */
public class JobManager implements AutoCloseable {

    private static final org.slf4j.Logger LOG =
            org.slf4j.LoggerFactory.getLogger(JobManager.class);

    public static final String DSJOB_PARALLEL = "dsjob.parallel";
    public static final String DSJOB_EXEC = "dsjob.exec";
    public static final String DSJOB_LIST = "dsjob.list";
    public static final String DSJOB_STATUS = "dsjob.status";
    public static final String DSJOB_RESET = "dsjob.reset";
    public static final String DSJOB_RUN = "dsjob.run";
    public static final String DSJOB_STOP = "dsjob.stop";

    private final String project;
    private final String jobType;
    private final String dsjobExec;
    private final String dsjobList;
    private final String dsjobStatus;
    private final String dsjobReset;
    private final String dsjobRun;
    private final String dsjobStop;

    private final List<String> dsjobExecList;

    private int parallelOps = 3;

    // Stable values for job start
    private String globalsId;
    private String batchId;
    private String inputDb;
    private String outputDb;

    private ExecutorService executor = null;

    public static String safeInvocation(String table) {
        return table.replace('.', '-').replaceAll("[\\[\\]\\\\\"']", "");
    }

    public JobManager(String project, String jobType, JobConfiguration conf) {
        this.project = project;
        this.jobType = jobType;
        this.dsjobExec = conf.getOption(DSJOB_EXEC);
        this.dsjobList = conf.getOption(DSJOB_LIST);
        this.dsjobStatus = conf.getOption(DSJOB_STATUS);
        this.dsjobReset = conf.getOption(DSJOB_RESET);
        this.dsjobRun = conf.getOption(DSJOB_RUN);
        this.dsjobStop = conf.getOption(DSJOB_STOP);
        this.dsjobExecList = new StringTokenizer(this.dsjobExec.trim())
                .getTokenList();
        String v = conf.getOption(DSJOB_PARALLEL);
        try {
            parallelOps = Integer.parseInt(v);
        } catch(NumberFormatException nfe) {
            parallelOps = 3;
        }
        if (parallelOps < 1 || parallelOps > 100)
            parallelOps = 3;
    }

    public int getParallelOps() {
        return parallelOps;
    }
    public void setParallelOps(int parallelOps) {
        this.parallelOps = parallelOps;
    }

    public String getGlobalsId() {
        return globalsId;
    }
    public void setGlobalsId(String globalsId) {
        this.globalsId = globalsId;
    }

    public String getBatchId() {
        return batchId;
    }
    public void setBatchId(String batchId) {
        this.batchId = batchId;
    }

    public String getInputDb() {
        return inputDb;
    }
    public void setInputDb(String inputDb) {
        this.inputDb = inputDb;
    }

    public String getOutputDb() {
        return outputDb;
    }
    public void setOutputDb(String outputDb) {
        this.outputDb = outputDb;
    }

    @Override
    public void close() throws Exception {
        if (executor!=null) {
            executor.shutdownNow();
            executor = null;
        }
    }

    public ExecutorService getExecutor() {
        if (executor == null) {
            if (parallelOps<1 || parallelOps>100)
                parallelOps = 3;
            executor = Executors.newFixedThreadPool(parallelOps);
        }
        return executor;
    }

    public List<JobInfo> listJobs() {
        final List<Future<JobInfo>> jobInfo = new ArrayList<>();
        for ( String jobId : new ListComm(this).list() ) {
            // Only handle the invocation with non-empty execution ID.
            if (! jobId.contains("."))
                continue;
            jobInfo.add( getExecutor().submit(new StatusComm(this, jobId)) );
        }
        final List<JobInfo> retval = new ArrayList<>();
        for ( Future<JobInfo> jif : jobInfo ) {
            try {
                JobInfo ji = jif.get();
                if (ji != null)
                    retval.add(ji);
            } catch(Exception ex) {
                LOG.warn("Failed to retrieve job status", ex);
            }
        }
        return retval;
    }

    public List<String> startJobs(List<JobDef> jobs) {
        final List<Future<String>> jobIds = new ArrayList<>();
        for (JobDef jd : jobs) {
            jobIds.add( getExecutor().submit(new RunComm(this, jd)) );
        }
        final List<String> retval = new ArrayList<>();
        for ( Future<String> jif : jobIds ) {
            try {
                retval.add(jif.get());
            } catch(Exception ex) {
                retval.add("");
                LOG.warn("Failed to start a job", ex);
            }
        }
        return retval;
    }

    public void stopJobs(List<String> jobs) {
        final List<Future<Boolean>> status = new ArrayList<>();
        for (String jobId : jobs) {
            status.add( getExecutor().submit(new StopComm(this, jobId)) );
        }
        final List<String> retval = new ArrayList<>();
        for ( Future<Boolean> jif : status ) {
            try {
                jif.get();
            } catch(Exception ex) {
                /* noop */
            }
        }
    }

    private static JobInfo grabJobInfo(String jid, List<String> rows) {
        if (rows.isEmpty())
            return null;
        String jobState = null;
        String jobTime = null;
        for (String row : rows) {
            if (row.startsWith("Job Status")) {
                int ix0 = row.indexOf(':');
                int ix1 = row.indexOf('(');
                if (ix0 > 0 && ix1 > ix0 && (ix1-ix0) > 2) {
                    jobState = row.substring(ix0+1, ix1-1).trim();
                }
            } else if (row.startsWith("Job Start Time")) {
                int ix0 = row.indexOf(':');
                if (ix0 > 0) {
                    jobTime = row.substring(ix0+1).trim();
                }
            }
        }
        if (jobState == null)
            return null;
        if ("RUNNING".equalsIgnoreCase(jobState)
                || "QUEUED".equalsIgnoreCase(jobState)) {
            final JobInfo ji = new JobInfo();
            ji.setJobId(jid);
            ji.setJobState(jobState);
            ji.setStartTime(jobTime);
            return ji;
        }
        return null;
    }

    /**
     * Gets the output and error streams from a process and reads them
     * to keep the process from blocking due to a full output buffer.
     * The processed stream data is appended to the supplied Appendable.
     * For this, two Threads are started, but join()ed, so we wait.
     * As implied by the waitFor... name, we also wait until we finish
     * as well. Finally, the input, output and error streams are closed.
     * Taken from Apache Groovy.
     *
     * @param self a Process
     * @param output an Appendable to capture the process stdout
     * @param error an Appendable to capture the process stderr
     */
    private static int waitForProcess(Process self, List<String> output, List<String> error) {
        Thread tout = new Thread(new TextDumper(self.getInputStream(), output));
        Thread terr = new Thread(new TextDumper(self.getErrorStream(), output));
        tout.start(); terr.start();
        boolean interrupted = false;
        int exitCode = -1;
        try {
            try { tout.join(); } catch (InterruptedException ignore) { interrupted = true; }
            try { terr.join(); } catch (InterruptedException ignore) { interrupted = true; }
            try { exitCode = self.waitFor(); } catch (InterruptedException ignore) { interrupted = true; }
            try { self.getErrorStream().close(); } catch (IOException ignore) {}
            try { self.getInputStream().close(); } catch (IOException ignore) {}
            try { self.getOutputStream().close(); } catch (IOException ignore) {}
        } finally {
            if (interrupted)
                Thread.currentThread().interrupt();
        }
        return exitCode;
    }

    private static void dumpOutput(String prefix, List<String> v) {
        if (v==null)
            return;
        for (String s : v) {
            if (s!=null && s.length() > 0)
                LOG.info("{} {}", prefix, s);
        }
    }

    /**
     * Dumps the text output from the process.
     */
    private static final class TextDumper implements Runnable {
        private final InputStream in;
        private final List<String> app;

        public TextDumper(InputStream in, List<String> app) {
            this.in = in;
            this.app = app;
        }

        @Override
        public void run() {
            InputStreamReader isr = new InputStreamReader(in);
            BufferedReader br = new BufferedReader(isr);
            String next;
            try {
                while ((next = br.readLine()) != null) {
                    if (app != null) {
                        app.add(next);
                    }
                }
            } catch (Exception e) {
                LOG.error("exception while reading process stream", e);
            }
        }
    }

    /**
     * Basic algorithms and data structures for all JobManager operations.
     */
    private static abstract class Commander {
        final JobManager owner;
        final List<String> currentCommand = new ArrayList<>();
        final List<String> workOutput = new ArrayList<>();
        final List<String> workErrors = new ArrayList<>();
        String currentDescription = null;

        public Commander(JobManager owner) {
            this.owner = owner;
        }

        void cmdInit(Map<String,String> m, String templ, String desc) {
            currentDescription = desc;
            currentCommand.clear();
            // Split the command into the operands.
            final List<String> splitCommand =
                    new StringTokenizer(templ.trim()).getTokenList();
            final StringSubstitutor replacer = new StringSubstitutor(m);
            for (String item : splitCommand) {
                if ("${dsjob}".equalsIgnoreCase(item)) {
                    // Special case for dsjob tool invocation.
                    currentCommand.addAll(owner.dsjobExecList);
                } else {
                    // Substitute variables into each operand.
                    currentCommand.add(replacer.replace(item));
                }
            }
        }

        int cmdRun(boolean allowFailure) {
            workOutput.clear();
            workErrors.clear();
            int exitCode = 0;
            String[] cmd = currentCommand.toArray
                (new String[currentCommand.size()]);
            if (currentDescription==null) {
                currentDescription = Arrays.toString(cmd);
            }
            try {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Running command {}", Arrays.toString(cmd));
                }
                Process proc = Runtime.getRuntime().exec(cmd);
                exitCode = waitForProcess(proc, workOutput, workErrors);
            } catch(Exception ex) {
                LOG.error("{} has FAILED to start", currentDescription, ex);
                throw new RuntimeException("Command for "
                        + currentDescription + " cannot be started");
            }
            if (exitCode == 0)
                return 0;
            if (allowFailure) {
                LOG.debug("{} has FAILED with status code {}",
                        currentDescription, exitCode);
                return exitCode;
            }
            LOG.error("{} has FAILED with status code {}",
                    currentDescription, exitCode);
            dumpOutput("OUT>", workOutput);
            dumpOutput("ERR>", workErrors);
            throw new RuntimeException("Command for " + currentDescription
                    + " has FAILED with status code " + exitCode);
        }

        void cmdRun() {
            cmdRun(false);
        }

        void cmdRun(Map<String,String> m, String templ, String desc) {
            cmdInit(m, templ, desc);
            cmdRun();
        }
    }

    /**
     * List jobs of the defined type.
     * Command format:
     *   dsjob -linvocations dstage1 MaskJdbc
     * Command output:
     *   one row per job invocation ID
     */
    private static final class ListComm extends Commander {
        public ListComm(JobManager owner) {
            super(owner);
        }
        public List<String> list() {
            final Map<String,String> subst = new HashMap<>();
            subst.put("project", owner.project);
            subst.put("jobType", owner.jobType);
            cmdRun(subst, owner.dsjobList, "List job invocations");
            return workOutput;
        }
    }

    /**
     * Retrieve the status of the job specified.
     * Command format:
     *   dsjob -jobinfo dstage1 $jobId
     * Command output:
     *   Job Status	: RUN OK (1)
     */
    private static final class StatusComm extends Commander
            implements Callable<JobInfo> {
        final String jobId;

        public StatusComm(JobManager owner, String jobId) {
            super(owner);
            this.jobId = jobId;
        }

        @Override
        public JobInfo call() throws Exception {
            final Map<String,String> subst = new HashMap<>();
            subst.put("project", owner.project);
            subst.put("jobId", jobId);
            cmdRun(subst, owner.dsjobStatus, "Retrieve job info");
            return grabJobInfo(jobId, workOutput);
        }
    }

    /**
     * Run the masking job.
     * Command format:
     *  $DSJOB -run -param BatchId="$BATCH_ID" -param Globals=default
     *   -param DbParams="$DB_SRC" -param DbOutParams="$DB_DST"
     *   -param InputTable="$TABNAME" -param OutputTable="$TABNAME"
     *   -param MaskingProfile="$TABPROF"
     *   dstage1 MaskJdbc."$INSTID"
     */
    private static final class RunComm extends Commander
            implements Callable<String> {
        final JobDef job;

        public RunComm(JobManager owner, JobDef job) {
            super(owner);
            this.job = job;
        }

        @Override
        public String call() throws Exception {
            String jobId = owner.jobType + "." +
                    safeInvocation(owner.inputDb + "." + job.inputTable);
            final Map<String,String> subst = new HashMap<>();
            subst.put("jobId", jobId);
            subst.put("project", owner.project);
            subst.put("globalsId", owner.globalsId);
            subst.put("batchId", owner.batchId);
            subst.put("dbIn", owner.inputDb);
            subst.put("dbOut", owner.outputDb);
            subst.put("tableIn", job.inputTable);
            subst.put("tableOut", job.outputTable);
            subst.put("profileId", job.maskingProfile);
            cmdInit(subst, owner.dsjobRun, "Start new job");
            final List<String> backupCommand = new ArrayList<>(currentCommand);
            int exitCode = cmdRun(true);
            if (exitCode != 0) {
                // Job might need a reset.
                // Command format:
                //   dsjob -run -mode RESET -wait dstage1 MaskJdbc."$INSTID"
                subst.clear();
                subst.put("jobId", jobId);
                subst.put("project", owner.project);
                cmdRun(subst, owner.dsjobReset, "Reset a failed job");
                // Re-running the original job
                currentCommand.clear();
                currentCommand.addAll(backupCommand);
                currentDescription = "Start new job after reset";
                cmdRun();
            }
            LOG.info("Started job {}", jobId);
            return jobId;
        }
    }

    /**
     * Stop the job with the ID specified.
     */
    private static final class StopComm extends Commander 
            implements Callable<Boolean> {
        final String jobId;

        public StopComm(JobManager owner, String jobId) {
            super(owner);
            this.jobId = jobId;
        }

        @Override
        public Boolean call() throws Exception {
            try {
                final Map<String,String> subst = new HashMap<>();
                subst.put("project", owner.project);
                subst.put("jobId", jobId);
                cmdRun(subst, owner.dsjobStop, "Stop a running job");
                LOG.info("Stopped job {}", jobId);
                return Boolean.TRUE;
            } catch(Exception ex) {
                LOG.warn("Failed to stop job {}", jobId, ex);
                return Boolean.FALSE;
            }
        }
    }

}
