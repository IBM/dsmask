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
package com.ibm.dsmask.jconf.fio;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import net.dsmask.algo.PureJavaCrc32;

/**
 * Compare two NamesBean values, according to their salted hashes.
 * @author mzinal
 */
public class NamesComparator implements Comparator<NamesBean> {

    private static final Charset CSET = StandardCharsets.UTF_8;

    private final byte[] sortSalt1;
    private final byte[] sortSalt2;
    private final PureJavaCrc32 crc = new PureJavaCrc32();

    public NamesComparator(String sortSalt1) {
        this.sortSalt1 = sortSalt1.getBytes(CSET);
        this.sortSalt2 = null;
    }

    public NamesComparator(byte[] sortSalt1) {
        this.sortSalt1 = sortSalt1;
        this.sortSalt2 = null;
    }

    public NamesComparator(String sortSalt1, String sortSalt2) {
        this.sortSalt1 = sortSalt1.getBytes(CSET);
        this.sortSalt2 = sortSalt2.getBytes(CSET);
    }

    public NamesComparator(byte[] sortSalt1, byte[] sortSalt2) {
        this.sortSalt1 = sortSalt1;
        this.sortSalt2 = sortSalt2;
    }

    @Override
    public int compare(NamesBean o1, NamesBean o2) {
        crc.reset();
        crc.update(o1.getSortKey(CSET));
        crc.update(sortSalt1);
        if (sortSalt2!=null)
            crc.update(sortSalt2);
        long v1 = crc.getValue();
        crc.reset();
        crc.update(o2.getSortKey(CSET));
        crc.update(sortSalt1);
        if (sortSalt2!=null)
            crc.update(sortSalt2);
        long v2 = crc.getValue();
        if (v1 == v2) {
            return o1.getName().compareToIgnoreCase(o2.getName());
        }
        return (v1 < v2) ? -1 : 1;
    }

}
