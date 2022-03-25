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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;

/**
 *
 * @author zinal
 */
public class UniqConnection implements Runnable {

    private static final org.slf4j.Logger LOG
            = org.slf4j.LoggerFactory.getLogger(UniqConnection.class);

    public static final int MAX_BATCH = 1000000;

    private final UniqSocketServer owner;
    private final Socket socket;
    private final DataInputStream dais;
    private final DataOutputStream daos;

    private final Object guard = new Object();
    private volatile boolean timeToQuit = false;

    private int numCalls = 0;
    private int numRequests = 0;
    private int numHints = 0;
    private int numRejects = 0;

    public UniqConnection(UniqSocketServer owner, Socket socket)
            throws Exception {
        this.owner = owner;
        this.socket = socket;
        this.dais = new DataInputStream(
                new BufferedInputStream(socket.getInputStream()));
        this.daos = new DataOutputStream(
                new BufferedOutputStream(socket.getOutputStream()));
    }

    public boolean isTimeToQuit() {
        synchronized(guard) {
            return timeToQuit;
        }
    }

    public void tellQuit() {
        synchronized(guard) {
            timeToQuit = true;
        }
    }

    @Override
    public void run() {
        LOG.info("UniqServer connected remote port {}, address {}",
                socket.getPort(), socket.getInetAddress());
        try {
            while (isTimeToQuit()==false) {
                if ( runStore() == false )
                    break; // connection closed or bad opcode
            }
        } catch(Exception ex) {
            LOG.error("UniqConnection exception", ex);
        }
        owner.sayGoodbye(this);
        LOG.info("UniqServer disconnected remote port {}, address {}, stats: "
                + "C={}, R={}, H={}, J={}",
                socket.getPort(), socket.getInetAddress(),
                numCalls, numRequests, numHints, numRejects);
        try {
            socket.close();
        } catch(Exception ex) {}
    }

    private boolean runStore() throws Exception {
        try {
            int val = socket.getInputStream().read();
            if (val < 0)
                return false;
            if (val != 123)
                throw new IllegalStateException("Bad operation markup octet");
        } catch(SocketTimeoutException ste) {
            return true;
        }
        // Receive and unpack the arguments
        String repository = dais.readUTF();
        int count = dais.readInt();
        if (count < 0 || count > MAX_BATCH) {
            throw new IllegalStateException("Protocol violation: "
                    + "req_count=" + count);
        }
        UniqRequest[] reqs = new UniqRequest[count];
        for (int i=0; i<count; ++i) {
            UniqRequest r = new UniqRequest();
            r.setIteration(dais.readInt());
            r.setSource(dais.readUTF());
            r.setTarget(dais.readUTF());
            r.setConflict(dais.readUTF());
            if (r.getConflict().length()==0)
                r.setConflict((String) null);
            reqs[i] = r;
        }
        // Make the service call
        UniqResponse[] resp = owner.getStore().store(repository, reqs);
        // Pack and send the results
        int hintCount = 0;
        int rejectCount = 0;
        daos.writeInt(count);
        for (UniqResponse r : resp) {
            daos.writeBoolean(r.isLinkedCorrectly());
            daos.writeInt(r.getIteration());
            if (r.isLinkedCorrectly()==false)
                rejectCount += 1;
            if (r.getIteration() > 0)
                hintCount += 1;
        }
        daos.flush();

        numCalls += 1;
        numRequests += reqs.length;
        numHints += hintCount;
        numRejects += rejectCount;
        return true;
    }

}
