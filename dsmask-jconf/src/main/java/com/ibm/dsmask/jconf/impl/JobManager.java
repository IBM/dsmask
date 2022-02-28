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
import com.ibm.dsmask.jconf.beans.JobInfo;
import java.util.Arrays;
import org.apache.commons.text.StringTokenizer;

/**
 * Masking job manager, part of MaskBatcher tool implementation.
 * @author zinal
 */
public class JobManager {

    private static final org.slf4j.Logger LOG =
            org.slf4j.LoggerFactory.getLogger(JobManager.class);

    private final String project;
    private final String jobType;
    private final String jobCommand;

    private List<String> jobCommandList;

    private final List<String> currentCommand = new ArrayList<>();
    private final List<String> workOutput = new ArrayList<>();
    private final List<String> workErrors = new ArrayList<>();
    private String currentDescription = null;

    public JobManager(String project, String jobType, String jobCommand) {
        this.project = project;
        this.jobType = jobType;
        this.jobCommand = jobCommand;
    }

    public List<JobInfo> listJobs() {
        // List the job invocations.
        // Command format:
        //   dsjob -linvocations dstage1 MaskJdbc
        // Command output:
        //   one row per job invocation ID
        initCommand()
            .addCommandItem("-linvocations")
            .addCommandItem(project)
            .addCommandItem(jobType)
            .executeCommand();
        final List<JobInfo> retval = new ArrayList<>();
        final List<String> invocations = new ArrayList<>(workOutput);
        // For each job invocation, grab the execution status.
        for (String invocation : invocations) {
            // Only handle the invocation with non-empty execution ID.
            if (! invocation.contains("."))
                continue;
            // Retrieve the job status.
            // Command format:
            //   dsjob -jobinfo dstage1 $jobId
            // Command output:
            //   Job Status	: RUN OK (1)
            initCommand()
                .addCommandItem("-jobinfo")
                .addCommandItem(project)
                .addCommandItem(invocation)
                .executeCommand();
            final JobInfo ji = grabJobInfo(invocation, workOutput);
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

    private List<String> getJobCommandList() {
        if (jobCommandList == null) {
            jobCommandList = new ArrayList<String>(
                    new StringTokenizer(jobCommand).getTokenList()
            );
        }
        return jobCommandList;
    }

    private JobManager initCommand() {
        currentDescription = null;
        currentCommand.clear();
        currentCommand.addAll(getJobCommandList());
        return this;
    }

    private JobManager setDescription(String v) {
        currentDescription = v;
        return this;
    }

    private JobManager addCommandItem(String v) {
        currentCommand.add(v);
        return this;
    }

    private void executeCommand() {
        workOutput.clear();
        workErrors.clear();
        int exitCode = 0;
        String[] cmd = currentCommand.toArray(new String[currentCommand.size()]);
        if (currentDescription==null) {
            currentDescription = Arrays.toString(cmd);
        }
        boolean failure = false;
        try {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Running command {}", Arrays.toString(cmd));
            }
            Process proc = Runtime.getRuntime().exec(cmd);
            exitCode = waitForProcess(proc, workOutput, workErrors);
        } catch(Exception ex) {
            LOG.error("{} has FAILED to start", currentDescription, ex);
            failure = true;
        }
        if (! failure) {
            if (exitCode != 0) {
                LOG.error("{} has FAILED with status code {}",
                        currentDescription, exitCode);
                dumpOutput("OUT>", workOutput);
                dumpOutput("ERR>", workErrors);
            }
        }
        if (failure) {
            throw new RuntimeException("Command for " + currentDescription + " has FAILED");
        }
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
            LOG.info("{} {}", prefix, v);
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
