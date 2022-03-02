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
package net.dsmask.uniq;

import java.io.FileInputStream;
import java.net.InetAddress;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import net.dsmask.DsMaskVersion;

/**
 *
 * @author zinal
 */
public class UniqService {

    private static final org.slf4j.Logger LOG
            = org.slf4j.LoggerFactory.getLogger(UniqService.class);

    public static final String PROP_WORK_DIR = "workDir";
    public static final String PROP_SHARD_COUNT = "shardCount";
    public static final String PROP_COMMIT_PERIOD = "lazyCommitPeriod";
    public static final String PROP_CACHE_SIZE_MB = "cacheSizeMB";
    public static final String PROP_SVC_PORT = "svcPort";
    public static final String PROP_SVC_HOST = "svcHost";
    public static final String PROP_SVC_SECRET = "svcSecret";

    public static void main(String[] args) {
        try {
            if (args.length != 1) {
                System.out.println("USAGE: " + UniqService.class.getName()
                        + " jobfile.xml");
                System.exit(1);
            }
            final Properties props = new Properties();
            try (FileInputStream fis = new FileInputStream(args[0])) {
                props.loadFromXML(fis);
            }

            LOG.info("Starting UniqService with job file {}", args[0]);
            new UniqService(props).run();

        } catch(Exception ex) {
            LOG.error("FATAL: operation failed", ex);
            System.exit(1);
        }
    }

    private final Properties props;
    private final CountDownLatch doneSignal = new CountDownLatch(1);
    private volatile UniqSocketServer server = null;

    public UniqService(Properties props) {
        this.props = props;
    }

    public void run() throws Exception {
        LOG.info("DsMask {} UniqService", DsMaskVersion.VERSION);
        // Handle shutdown signal
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                if (server!=null) {
                    LOG.info("Got shutdown request...");
                    server.tellQuit();
                    try {
                        doneSignal.await();
                    } catch(InterruptedException ix) {}
                }
            }
        });
        // Open the store
        try (UniqStore store = new UniqStore(
                getWorkDir(),
                getShardCount(),
                getCacheSize(),
                getCommitPeriod()
        )) {
            store.open();
            // Prepare and start the socket server
            server = new UniqSocketServer(
                    getPortNumber(),
                    getAddress(),
                    getSecret(),
                    store);
            LOG.info("Started service at port " + server.getPortNumber());
            server.run();
            LOG.info("Service shutting down...");
        }
        LOG.info("Service terminated!");
        // Allow the shutdown hook to exit
        doneSignal.countDown();
    }

    private int getPortNumber() throws Exception {
        return Integer.parseInt(props.getProperty(PROP_SVC_PORT, "27501"));
    }

    private int getCacheSize() throws Exception {
        return Integer.parseInt(props.getProperty(PROP_CACHE_SIZE_MB, "4"));
    }

    private int getShardCount() throws Exception {
        return Integer.parseInt(props.getProperty(PROP_SHARD_COUNT, "10"));
    }

    private int getCommitPeriod() throws Exception {
        return Integer.parseInt(props.getProperty(PROP_COMMIT_PERIOD, "10"));
    }

    private String getWorkDir() {
        return props.getProperty(PROP_WORK_DIR, "shards");
    }

    private String getSecret() {
        return props.getProperty(PROP_SVC_SECRET);
    }

    private InetAddress getAddress() throws Exception {
        String svcHost = props.getProperty(PROP_SVC_HOST);
        InetAddress address = null;
        if (svcHost!=null && svcHost.length() > 0)
            address = InetAddress.getByName(svcHost);
        return address;
    }

}
