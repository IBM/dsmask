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
package com.ibm.dsmask.algo;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.apache.commons.lang3.StringUtils;

/**
 * Compute a specified type of "hash" (message digest) on input.
 * @author zinal
 */
public class BasicDigest {

    private final byte[] userKeyBytes;
    private final MessageDigest md;

    public BasicDigest(String hashType, byte[] userKeyBytes) {
        this.userKeyBytes = userKeyBytes;
        if (hashType==null || hashType.length()==0)
            hashType = "SHA-512";
        try {
            this.md = MessageDigest.getInstance(hashType);
        } catch(NoSuchAlgorithmException nsae) {
            throw new IllegalArgumentException(nsae);
        }
    }

    public BasicDigest() {
        this(null, (byte[]) null);
    }

    public BasicDigest(byte[] userKeyBytes) {
        this(null, userKeyBytes);
    }

    public BasicDigest(String hashType, String userKey) {
        this(hashType, (userKey==null) ? (byte[]) null
                : userKey.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Calculate the digest value in its binary form.
     * On input, arrays of bytes are treated as-is.
     * All other data types are converted to character strings,
     * and interpreted in UTF-8 encoding.
     * @param values Input values
     * @param iteration Iteration number (0 - initial/unaltered iteration)
     * @return Digest value
     */
    public byte[] calcBinary(Object[] values, int iteration) {
        if (values==null || values.length==0)
            return null;
        md.reset();
        boolean sign = false;
        if (iteration > 0) {
            md.update(Integer.toHexString(iteration)
                    .getBytes(StandardCharsets.UTF_8));
            sign = true;
        }
        for ( Object v : values ) {
            if (sign)
                md.update((byte) 1);
            else
                sign = true;
            update(v);
        }
        if (userKeyBytes!=null && userKeyBytes.length > 0)
            md.update(userKeyBytes);
        return md.digest();
    }

    /**
     * Convert array of bytes to its hexadecimal representation.
     * @param input Array of bytes
     * @return String containing two hex characters per input byte
     */
    public static String toHex(byte[] input) {
        if (input==null)
            return null;
        if (input.length==0)
            return "";
        String hashHex = new BigInteger(1, input).toString(16);
        return StringUtils.leftPad(hashHex, input.length * 2, '0');
    }

    /**
     * Calculate the digest value in its hexadecimal form.
     * @param values Input values
     * @param iteration Iteration number (0 - initial/unaltered iteration)
     * @return Digest value
     */
    public String calcHex(Object[] values, int iteration) {
        return toHex( calcBinary(values, iteration) );
    }

    private void update(Object value) {
        if (value!=null) {
            if (value instanceof byte[])
                md.update((byte[]) value);
            else
                md.update(value.toString().getBytes(StandardCharsets.UTF_8));
        }
    }

}
