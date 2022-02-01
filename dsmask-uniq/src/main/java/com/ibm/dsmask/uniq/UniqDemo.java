/*
 * Copyright (c) IBM Corp. 2018, 2021.
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
package com.ibm.dsmask.uniq;

import java.io.File;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.UUID;
import com.ibm.dsmask.algo.MaskFPE;

/**
 * Demonstration and basic performance test for dsmask-uniq module.
 * @author zinal
 */
public class UniqDemo {

    private static final org.slf4j.Logger LOG
            = org.slf4j.LoggerFactory.getLogger(UniqDemo.class);

    public static void main(String[] args) {
        try {
            String mode = "net";
            if (args.length > 0)
                mode = args[0];
            new UniqDemo("net".equalsIgnoreCase(mode)) . run();
        } catch(Exception ex) {
            ex.printStackTrace(System.out);
            System.exit(1);
        }
    }

    public static final String STORE_TYPE = "aaa";
    public static final String STORE_SECRET = "abcdefg";

    public final boolean networkMode;

    public int numTotal = 0;
    public int numHint = 0;
    public int numStore = 0;
    public int numFail = 0;
    public int numIter = 0;

    public UniqDemo(boolean networkMode) {
        this.networkMode = networkMode;
    }

    public void run() throws Exception {
        Path dir = Files.createTempDirectory("uniqdemo");
        System.out.println("Working in " + dir);
        final long tvStart = System.currentTimeMillis();
        long tvFinish = 0;
        try {
            try (UniqProvider provider = openProvider(dir.toFile())) {
                final Worker[] workers = new Worker[10];
                for (int i=0; i<workers.length; ++i)
                    workers[i] = new Worker(provider);
                final Thread[] threads = new Thread[workers.length];
                for (int i=0; i<workers.length; ++i) {
                    threads[i] = new Thread(workers[i]);
                    threads[i].setDaemon(true);
                    threads[i].setName("uniq-demo-worker-" + i);
                    threads[i].start();
                }
                for (Thread t : threads) {
                    t.join();
                }
                tvFinish = System.currentTimeMillis();
                for (Worker w : workers) {
                    numTotal += w.numTotal;
                    numHint += w.numHint;
                    numStore += w.numStore;
                    numFail += w.numFail;
                    numIter += w.numIter;
                }
            }
            Files.walk(dir).forEach((Path t) -> {
                File f = t.toFile();
                if (f.isFile())
                    System.out.println(" - " + f.getName() + " size "
                            + f.length() + " bytes");
            });
        } finally {
            Files.walk(dir).sorted(Comparator.reverseOrder())
                    .map(Path::toFile).forEach(File::delete);
        }

        LOG.info("Completed, total runtime: {}", (tvFinish-tvStart));
        LOG.info("** total:  {}", numTotal);
        LOG.info("** store:  {}", numStore);
        LOG.info("** fail:   {}", numFail);
        LOG.info("** hint:   {}", numHint);
        LOG.info("** iter:   {}", numIter);
    }

    private UniqProvider openProvider(File workPath) throws Exception {
        UniqStore store = new UniqStore(workPath, 10, 5, 10);
        store.open();
        if (networkMode) {
            UniqSocketServer service = new UniqSocketServer(10500, null, STORE_SECRET, store);
            Thread st = new Thread(service);
            st.setDaemon(true);
            st.setName("uniq-store-network-service");
            st.start();
            Thread.sleep(1000L);
            return new MegaClient(service, st, STORE_SECRET);
        } else {
            return store;
        }
    }

    public static String makeNewValue() {
        String v = UUID.randomUUID().toString().replaceAll("-", "")
                .substring(5, 9);
        return v;
    }

    private static class MegaClient implements UniqProvider {

        private final UniqSocketServer service;
        private final Thread serviceThread;
        private final ThreadLocal<UniqClient> client;

        public MegaClient(UniqSocketServer service, Thread serviceThread,
                String secret) throws Exception {
            this.service = service;
            this.serviceThread = serviceThread;
            this.client = new ThreadLocal<UniqClient>() {
                @Override
                protected UniqClient initialValue() {
                    try {
                        return new UniqClient(InetAddress.getLoopbackAddress(),
                                service.getPortNumber(), secret);
                    } catch(Exception ex) {
                        throw new RuntimeException("", ex);
                    }
                }
            };
        }

        @Override
        public void close() {
            service.tellQuit();
            try { serviceThread.join(); } catch(InterruptedException ix) {}
            service.getStore().close();
        }

        @Override
        public UniqResponse[] store(String repository, UniqRequest[] rr) {
            return client.get().store(repository, rr);
        }
    } // class MegaClient

    private static class Worker implements Runnable {
        private final UniqProvider provider;
        private final MaskFPE masker;

        public static final int NUM_VALUES = 30000;

        public int numTotal = 0;
        public int numHint = 0;
        public int numStore = 0;
        public int numFail = 0;
        public int numIter = 0;

        public Worker(UniqProvider provider) {
            this.provider = provider;
            this.masker = new MaskFPE();
        }

        @Override
        public void run() {
            long tvProgress = System.currentTimeMillis();
            int counterProgress = 0;

            Object[] arrSource = new Object[1];
            Object[] arrTarget = new Object[1];
            Object[] arrConflict = new Object[1];

            final UniqRequest urq = new UniqRequest();
            final UniqRequest[] urq_arr = new UniqRequest[] { urq };

            for ( int i=0; i<NUM_VALUES; ++i) {
                String value = makeNewValue();
                ++numTotal;
                int iteration = 0;
                String initMasked = null;
                boolean hasHint = false;
                while (true) {
                    ++numIter;

                    if (counterProgress++ >= 100) {
                        counterProgress = 0;
                        long tvCur = System.currentTimeMillis();
                        if (tvCur - tvProgress >= 15000L) {
                            tvProgress = tvCur;
                            LOG.info("Thread {} processed {} of {}",
                                    Thread.currentThread().getName(),
                                    i, NUM_VALUES);
                        }
                    }

                    // Compute the next masked value
                    String masked = masker.calculate(value, iteration);
                    // Always store the masked value for the first iteration,
                    // it needs to be passed on further iterations as
                    // a "conflict" parameter.
                    if (initMasked==null) {
                        initMasked = masked;
                    }
                    urq.setIteration(iteration);
                    arrSource[0] = value;
                    urq.setSource(arrSource);
                    arrTarget[0] = masked;
                    urq.setTarget(arrTarget);
                    if (iteration==0) {
                        urq.setConflict((String) null);
                    } else {
                        urq.setConflict(arrConflict);
                        arrConflict[0] = initMasked;
                    }
                    UniqResponse[] resp = provider.store(STORE_TYPE, urq_arr);
                    if (resp[0].isLinkedCorrectly()) {
                        ++numStore;
                        break;
                    }
                    if (hasHint) {
                        // We had a hint, and still failed - pretty strange.
                        LOG.warn("Non-working hint #" + iteration
                            + " for source [" + value + "], "
                            + "conflict on [" + initMasked + "]");
                    }
                    if (resp[0].getIteration() > 0) {
                        // We got a hint which iteration number to use
                        ++numHint;
                        iteration = resp[0].getIteration();
                        hasHint = true;
                    } else {
                        ++iteration;
                        if (iteration > 10000) {
                            ++numFail;
                            break;
                        }
                    }
                } // while (true)
            }
        }
    }

}
