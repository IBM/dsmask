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
package net.dsmask.algo;

import java.nio.charset.StandardCharsets;

/**
 * Compute a single numerical hash based on CRC-32 algorithm.
 * @author zinal
 */
public class BasicHasher {

    private final byte[] userKeyBytes;
    private final int[] indexes;

    public BasicHasher(byte[] userKeyBytes, int[] indexes) {
        this.userKeyBytes = userKeyBytes;
        this.indexes = indexes;
    }

    public BasicHasher() {
        this((byte[]) null, null);
    }

    public BasicHasher(String userKey, int[] indexes) {
        this((userKey==null) ? (byte[]) null
                : userKey.getBytes(StandardCharsets.UTF_8), indexes);
    }

    public BasicHasher(String userKey) {
        this(userKey, null);
    }

    /**
     * Compute the hash value.
     * @param vec Input data vector
     * @param iteration Current iteration number
     * @return Hash value (based on CRC-32)
     */
    public final long calcHash(Object[] vec, int iteration) {
        final PureJavaCrc32 crc = new PureJavaCrc32();
        boolean sign = false;
        if (iteration>0) {
            crc.update(Integer.toHexString(iteration)
                    .getBytes(StandardCharsets.UTF_8));
            sign = true;
        }
        if (indexes==null) {
            for (Object o : vec) {
                if (sign)
                    crc.update(1);
                else
                    sign = true;
                update(crc, o);
            }
        } else {
            for (int i : indexes) {
                if (sign)
                    crc.update(1);
                else
                    sign = true;
                if (i>0 && i<=vec.length) { // just skip illegal indexes
                    update(crc, vec[i-1]);
                }
            }
        }
        if (userKeyBytes!=null && userKeyBytes.length > 0)
            crc.update(userKeyBytes);
        final long v = crc.getValue();
        return (v>=0) ? v : (-1L * v);
    }

    private static void update(PureJavaCrc32 crc, Object o) {
        if (o!=null) {
            if ( o instanceof byte[] )
                crc.update((byte[]) o);
            else
                crc.update(o.toString().getBytes(StandardCharsets.UTF_8));
        }
    }

}
