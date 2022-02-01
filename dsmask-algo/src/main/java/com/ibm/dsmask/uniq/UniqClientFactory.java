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

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 *
 * @author zinal
 */
public class UniqClientFactory implements UniqProviderFactory {

    private final InetAddress address;
    private final int port;
    private final String secret;

    public UniqClientFactory(InetAddress address, int port, String secret) {
        this.address = address;
        this.port = port;
        this.secret = secret;
    }

    public UniqClientFactory(String address, int port, String secret) {
        try {
            this.address = InetAddress.getByName(address);
        } catch(UnknownHostException uhe) {
            throw new RuntimeException(uhe);
        }
        this.port = port;
        this.secret = secret;
    }

    @Override
    public UniqProvider createProvider() {
        return new UniqClient(address, port, secret);
    }

}
