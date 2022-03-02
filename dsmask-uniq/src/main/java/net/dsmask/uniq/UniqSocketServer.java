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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import net.dsmask.util.SimpleDiegester;

/**
 * Service to ensure that unique masked value is mapped
 * to each unique input value
 * @author zinal
 */
public class UniqSocketServer implements Runnable, AutoCloseable {

    private static final org.slf4j.Logger LOG
            = org.slf4j.LoggerFactory.getLogger(UniqSocketServer.class);

    private final int portNumber;
    private final InetAddress address;
    private final UniqStore store;

    private final ServerSocket serverSocket;
    private final List<UniqConnection> connections = new ArrayList<>();
    private final SimpleDiegester diegester;

    private final Object guard = new Object();
    private volatile boolean timeToQuit = false;

    public UniqSocketServer(int portNumber, InetAddress address, String secret,
            UniqStore store) throws Exception {
        this.portNumber = portNumber;
        this.address = address;
        this.store = store;
        this.diegester = new SimpleDiegester(secret);
        this.serverSocket = new ServerSocket(portNumber, 50, address);
        this.serverSocket.setSoTimeout(200);
    }

    public int getPortNumber() {
        return portNumber;
    }

    public InetAddress getAddress() {
        return address;
    }

    public UniqStore getStore() {
        return store;
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

    public void sayGoodbye(UniqConnection uc) {
        synchronized(connections) {
            connections.remove(uc);
        }
    }

    @Override
    public void run() {
        LOG.info("UniqServer running on port {}, address {}", portNumber, address);
        while (isTimeToQuit() == false) {
            try {
                Socket socket;
                try {
                    socket = serverSocket.accept();
                } catch(SocketTimeoutException ste) {
                    continue;
                }
                socket.setKeepAlive(true);
                socket.setTcpNoDelay(true);
                socket.setSoTimeout(1000);
                if (!diegest(socket)) {
                    LOG.warn("UniqServer rejected remote port {}, address {}",
                            socket.getPort(), socket.getInetAddress());
                    socket.close();
                } else {
                    UniqConnection uc = new UniqConnection(this, socket);
                    Thread t = new Thread(uc);
                    t.setDaemon(false);
                    t.setName("uniq-service-client#" + socket.getPort());
                    t.start();
                    synchronized(connections) {
                        connections.add(uc);
                    }
                }
            } catch(Exception ex) {
                LOG.warn("UniqServer exception", ex);
            }
        }
        LOG.info("UniqServer shutting down...");
        final List<UniqConnection> temp;
        synchronized(connections) {
            temp = new ArrayList<>(connections);
            connections.clear();
        }
        for (UniqConnection uc : temp) {
            uc.tellQuit();
        }
    }

    private boolean diegest(Socket socket) throws Exception {
        String serverRequest = diegester.makeRequest();
        String serverResponse = diegester.makeResponse(serverRequest);
        try {
            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
            dos.writeUTF(serverRequest);
            dos.flush();
            DataInputStream dis = new DataInputStream(socket.getInputStream());
            String clientResponse = dis.readUTF();
            if ( serverResponse.equals(clientResponse) ) {
                dos.writeByte(1);
                dos.flush();
                return true;
            } else {
                dos.writeByte(2);
                dos.flush();
                return false;
            }
        } catch(Exception ex) {
            LOG.warn("Diegester exception", ex);
            return false;
        }
    }

    @Override
    public void close() throws Exception {
        tellQuit();
    }

}
