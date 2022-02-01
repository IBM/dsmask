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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author mzinal
 */
public class NamesSorter {

    private final NamesComparator cmp;

    public NamesSorter(String salt) {
        this.cmp = new NamesComparator(salt);
    }

    public NamesSorter(byte[] salt) {
        this.cmp = new NamesComparator(salt);
    }

    /**
     * Sort the lists in NamesData, according to the salted hash value,
     * and return the new sorted copy.
     * We assume that there are no duplicates in the input
     * (this is checked on load, see readSimple and readExtended methods).
     * @param nd Input lists
     * @return Output - sorted lists
     */
    public NamesData sort(NamesData nd) {
        final NamesData ret = new NamesData();
        ret.first = sort(nd.first);
        ret.last = sort(nd.last);
        ret.middle = sort(nd.middle);
        return ret;
    }

    /**
     * Sort the list of values according to salted hash values.
     * @param src Input list with no duplicate values
     * @return New copy, sorted appropriately.
     */
    public List<NamesBean> sort(List<NamesBean> src) {
        final List<NamesBean> ret = new ArrayList<>(src);
        Collections.sort(ret, cmp);
        return ret;
    }

}
