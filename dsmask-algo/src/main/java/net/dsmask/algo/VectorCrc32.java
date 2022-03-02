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
 *
 * @author zinal
 */
public class VectorCrc32 {

    private final byte[] userKeyBytes;
    private final long maxHash;

    public VectorCrc32(String userKey, long maxHash) {
        this.userKeyBytes = (userKey==null) ? null :
                userKey.getBytes(StandardCharsets.UTF_8);
        this.maxHash = maxHash;
    }

    public VectorCrc32(byte[] userKeyBytes, long maxHash) {
        this.userKeyBytes = userKeyBytes;
        this.maxHash = maxHash;
    }

    public long calculate(Object o) {
        if (o==null)
            return -1L;
        String value = o.toString();
        if (value.length()==0)
            return -2L;
        final PureJavaCrc32 crc = new PureJavaCrc32();
        crc.update(value.getBytes(StandardCharsets.UTF_8));
        crc.update(2);
        if (userKeyBytes != null && userKeyBytes.length > 0)
            crc.update(userKeyBytes);
        return crc.getValue() % maxHash;
    }

    public long calculate(Object[] values) {
        if (values==null || values.length==0)
            return -1L;
        if (allNulls(values))
            return -1L; // This includes empty input
        final PureJavaCrc32 crc = new PureJavaCrc32();
        for (Object value : values) {
            if (value != null) {
                String strValue = value.toString();
                if (strValue.length() > 0)
                    crc.update(strValue.getBytes(StandardCharsets.UTF_8));
            }
            crc.update(2);
        }
        if (userKeyBytes != null && userKeyBytes.length > 0)
            crc.update(userKeyBytes);
        return crc.getValue() % maxHash;
    }

    private static boolean allNulls(Object[] values) {
        for (Object v : values)
            if (v!=null)
                return false;
        return true;
    }

}
