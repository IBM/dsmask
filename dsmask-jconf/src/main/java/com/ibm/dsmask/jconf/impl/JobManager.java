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
import org.apache.commons.text.StringSubstitutor;
import org.apache.commons.text.StringTokenizer;
import com.ibm.dsmask.jconf.beans.JobInfo;

/**
 * Masking job manager, part of MaskBatcher tool implementation.
 * @author zinal
 */
public class JobManager {

    private static final org.slf4j.Logger LOG =
            org.slf4j.LoggerFactory.getLogger(JobManager.class);

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

    // Stable values for job start
    private String globalsId;
    private String batchId;
    private String inputDb;
    private String outputDb;

    private final List<String> currentCommand = new ArrayList<>();
    private final List<String> workOutput = new ArrayList<>();
    private final List<String> workErrors = new ArrayList<>();
    private String currentDescription = null;

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

    public List<JobInfo> listJobs() {
        final Map<String,String> subst = new HashMap<>();
        // List the job invocations.
        // Command format:
        //   dsjob -linvocations dstage1 MaskJdbc
        // Command output:
        //   one row per job invocation ID
        subst.clear();
        subst.put("dsjob", dsjobExec);
        subst.put("project", project);
        subst.put("jobType", jobType);
        initCommand(subst, dsjobList, "List job invocations")
            .executeCommand();
        final List<JobInfo> retval = new ArrayList<>();
        final List<String> invocations = new ArrayList<>(workOutput);
        // For each job invocation, grab the execution status.
        for (String jobId : invocations) {
            // Only handle the invocation with non-empty execution ID.
            if (! jobId.contains("."))
                continue;
            // Retrieve the job status.
            // Command format:
            //   dsjob -jobinfo dstage1 $jobId
            // Command output:
            //   Job Status	: RUN OK (1)
            subst.clear();
            subst.put("dsjob", dsjobExec);
            subst.put("project", project);
            subst.put("jobId", jobId);
            initCommand(subst, dsjobStatus, "Retrieve job info")
                .executeCommand();
            final JobInfo ji = grabJobInfo(jobId, workOutput);
            if (ji != null)
                retval.add(ji);
        }
        return retval;
    }

    private JobInfo grabJobInfo(String jid, List<String> rows) {
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

    public String startJob(String tabIn, String tabOut, String profileId) {
        final Map<String,String> subst = new HashMap<>();
        final String retval = jobType + "." +
                safeInvocation(inputDb + "." + tabIn);
        // Command format:
	// $DSJOB -run -param BatchId="$BATCH_ID" -param Globals=default
	//  -param DbParams="$DB_SRC" -param DbOutParams="$DB_DST"
	//  -param InputTable="$TABNAME" -param OutputTable="$TABNAME"
	//  -param MaskingProfile="$TABPROF"
	//  dstage1 MaskJdbc."$INSTID"
        subst.clear();
        subst.put("dsjob", dsjobExec);
        subst.put("globalsId", globalsId);
        subst.put("batchId", batchId);
        subst.put("dbIn", inputDb);
        subst.put("dbOut", outputDb);
        subst.put("tableIn", tabIn);
        subst.put("tableOut", tabOut);
        subst.put("profileId", profileId);
        subst.put("jobId", retval);
        subst.put("project", project);
        initCommand(subst, dsjobRun, "Start new job");
        final List<String> backupCommand = new ArrayList<>(currentCommand);
        int exitCode = executeCommand(true);
        if (exitCode != 0) {
            // Job might need a reset.
            // Command format:
            //   dsjob -run -mode RESET -wait dstage1 MaskJdbc."$INSTID"
            subst.clear();
            subst.put("dsjob", dsjobExec);
            subst.put("jobId", retval);
            subst.put("project", project);
            initCommand(subst, dsjobReset, "Reset a failed job")
                .executeCommand();
            // Re-running the original job
            currentCommand.clear();
            currentCommand.addAll(backupCommand);
            currentDescription = "Start new job after reset";
            executeCommand();
        }
        return retval;
    }

    public void stopJob(String jobId) {
        final Map<String,String> subst = new HashMap<>();
        subst.put("dsjob", dsjobExec);
        subst.put("project", project);
        subst.put("jobId", jobId);
        initCommand(subst, dsjobStop, "Stop a running job")
            .executeCommand();
    }

    private JobManager initCommand(Map<String,String> m, String templ, String desc) {
        final String text = new StringSubstitutor(m).replace(templ).trim();
        currentDescription = desc;
        currentCommand.clear();
        currentCommand.addAll(new ArrayList<>(
                new StringTokenizer(text).getTokenList()
        ));
        return this;
    }

    private int executeCommand(boolean allowFailure) {
        workOutput.clear();
        workErrors.clear();
        int exitCode = 0;
        String[] cmd = currentCommand.toArray(new String[currentCommand.size()]);
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

    private void executeCommand() {
        executeCommand(false);
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

}
