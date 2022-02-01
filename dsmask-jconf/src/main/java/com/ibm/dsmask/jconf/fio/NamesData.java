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
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author mzinal
 */
public class NamesData {

    public List<NamesBean> first;
    public List<NamesBean> last;
    public List<NamesBean> middle;

    public NamesData() {
    }

    public NamesData(NamesData nd) {
        this.first = new ArrayList<>(nd.first);
        this.last = new ArrayList<>(nd.last);
        this.middle = new ArrayList<>(nd.middle);
    }

    public NamesData[] split(int n) {
        if (n<2) {
            return new NamesData[] { new NamesData(this) };
        }
        // Compute the number of elements in each bin.
        // Trying to make bin sizes equal.
        // The algorithm is not optimal, but with dictionary sizes
        // less than 1000... it does not matter at all.
        int ntotal = middle.size();
        int[] neach = new int[n];
        Arrays.fill(neach, 1);
        int ndone = n;
        int pos = 0;
        while (ndone < ntotal) {
            neach[pos] += 1;
            ndone += 1;
            pos += 1;
            if (pos >= n)
                pos = 0;
        }
        if (ndone > ntotal)
            throw new RuntimeException("Insufficient amount of middle names");
        // Generate the bins
        NamesData[] ret = new NamesData[n];
        pos = 0;
        for (int i=0; i<n; ++i) {
            ret[i] = new NamesData();
            ret[i].first = new ArrayList<>(this.first);
            ret[i].last = new ArrayList<>(this.last);
            ret[i].middle = new ArrayList<>(neach[i]);
            for (int j=0; j<neach[i]; ++j) {
                ret[i].middle.add(this.middle.get(pos++));
            }
        }
        return ret;
    }

}
