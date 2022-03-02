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
package net.dsmask.util;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import org.apache.commons.text.RandomStringGenerator;
import org.apache.commons.text.TextRandomProvider;

/**
 * Simple digest-like authentication implementation.
 * The server generates a random string, which is passed to the client.
 * The client computes a MAC of the received string, using the secret password.
 * The server checks whether its own MAC value matches the value it received
 * from the client (which means that both server and client use the same
 * value of the secret password).
 * @author zinal
 */
public class SimpleDiegester {

    // MAC for authentication
    public static final String HMAC_NAME = "HmacSHA512";

    // The length of request is constant
    public static final int REQ_LEN = 40;

    private final byte[] userKey;
    private final Mac mac;
    private final RandomStringGenerator stringGen;

    public SimpleDiegester(byte[] userKey, TextRandomProvider random)
            throws Exception {
        if (userKey==null || userKey.length==0)
            userKey = "P@ssw0rd".getBytes(StandardCharsets.UTF_8);
        if (random==null)
            random = new SecureRandom()::nextInt;
        this.userKey = userKey;
        this.mac = Mac.getInstance(HMAC_NAME);
        this.mac.init(new SecretKeySpec(userKey, HMAC_NAME));
        this.stringGen = new RandomStringGenerator.Builder()
                .usingRandom(random)
                .withinRange(new char[][] {
                    { 'a', 'z' },
                    { 'A', 'Z' },
                    { '0', '9' },
                    { 'а', 'я' },
                    { 'А', 'Я' } })
                .build();
    }

    public SimpleDiegester(byte[] userKey) throws Exception {
        this(userKey, null);
    }

    public SimpleDiegester(String userKey, TextRandomProvider random)
            throws Exception {
        this(userKey==null ?
                null : userKey.getBytes(StandardCharsets.UTF_8), random);
    }

    public SimpleDiegester(String userKey) throws Exception {
        this(userKey, null);
    }

    public SimpleDiegester() throws Exception {
        this((byte[]) null, null);
    }

    public byte[] getUserKey() {
        return userKey;
    }

    public String makeRequest() {
        return stringGen.generate(REQ_LEN);
    }

    public String makeResponse(String request) {
        byte[] hash = mac.doFinal(request.getBytes(StandardCharsets.UTF_8));
        return DatatypeConverter.printBase64Binary(hash);
    }

}
