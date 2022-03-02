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

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import net.dsmask.util.SimpleDiegester;

/**
 *
 * @author zinal
 */
public class UniqClient implements UniqProvider {

    private final Socket socket;
    private final DataInputStream dais;
    private final DataOutputStream daos;

    public UniqClient(InetAddress address, int portNumber, String secret) {
        Socket theSocket = null;
        try {
            theSocket = new Socket(address, portNumber);
            theSocket.setKeepAlive(true);
            theSocket.setTcpNoDelay(true);
            this.dais = new DataInputStream(theSocket.getInputStream());
            this.daos = new DataOutputStream(
                    new BufferedOutputStream(theSocket.getOutputStream()));
            diegest(this.dais, this.daos, secret);
            this.socket = theSocket;
        } catch(Exception ex) {
            try {
                if (theSocket!=null)
                    theSocket.close();
            } catch(Exception any) {}
            throw new RuntimeException(ex);
        }
    }

    @Override
    public UniqResponse[] store(String repository, UniqRequest[] rr) {
        if (rr==null || rr.length==0)
            return new UniqResponse[0];
        try {
            daos.write(123);
            daos.writeUTF(repository);
            daos.writeInt(rr.length);
            for (UniqRequest r : rr) {
                daos.writeInt(r.getIteration());
                daos.writeUTF(r.getSource());
                daos.writeUTF(r.getTarget());
                if (r.getConflict()==null)
                    daos.writeUTF("");
                else
                    daos.writeUTF(r.getConflict());
            }
            daos.flush();
            int count = dais.readInt();
            if (count != rr.length) {
                throw new IllegalStateException("Protocol violation: "
                        + "expected size of " + rr.length + ", got "
                        + count);
            }
            UniqResponse[] response = new UniqResponse[rr.length];
            for (int i=0; i<rr.length; ++i) {
                response[i] = new UniqResponse(
                        dais.readBoolean(),
                        dais.readInt());
            }
            return response;
        } catch(Exception ex) {
            throw new RuntimeException("UniqClient.store() failed", ex);
        }
    }

    @Override
    public void close() {
        try { socket.close(); } catch(Exception ex) {}
    }

    private static void diegest(DataInputStream dais, DataOutputStream daos,
            String secret) throws Exception {
        String serverRequest = dais.readUTF();
        String clientResponse = new SimpleDiegester(secret)
                .makeResponse(serverRequest);
        daos.writeUTF(clientResponse);
        daos.flush();
        byte status = dais.readByte();
        if (status!=1) {
            throw new Exception("Diegest failed, server denied access");
        }
    }

}
