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
package com.ibm.dsmask.ops;

import com.ibm.dsmask.impl.*;

/**
 *
 * @author zinal
 */
public class AlDefaultVector implements AlSimpleVector, StatsDumper {

    private final AlSimpleValue simple;

    public AlDefaultVector(AlSimpleValue simple) {
        this.simple = simple;
    }

    @Override
    public XVector exec(XVector in, XVector out) {
        return exec(in, out, 0);
    }

    @Override
    public void dumpStats(StringBuilder sb) {
        if (simple instanceof StatsDumper)
            ((StatsDumper)simple).dumpStats(sb);
    }

    @Override
    public boolean isIterationsSupported() {
        return simple.isIterationsSupported();
    }

    @Override
    public XVector exec(XVector in, XVector out, int iteration) {
        if (in==null)
            return null;
        out = XVector.make(out, in.values.length);
        if (simple.isIterationsSupported()) {
            for (int i=0; i<in.values.length; ++i)
                out.values[i] = simple.exec(in.values[i], iteration);
        } else {
            for (int i=0; i<in.values.length; ++i)
                out.values[i] = simple.exec(in.values[i]);
        }
        return out;
    }

}
