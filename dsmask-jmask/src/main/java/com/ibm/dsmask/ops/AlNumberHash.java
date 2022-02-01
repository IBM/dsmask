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

import java.util.List;
import com.ibm.dsmask.impl.*;
import com.ibm.dsmask.beans.MskFunc;
import com.ibm.dsmask.algo.BasicHasher;

/**
 * Hash value supporting algorithm.
 * Can be used in more complex repeatable hashing algorithms.
 * @author zinal
 */
public class AlNumberHash implements AlSimpleVector {

    private final String userKey;
    private final long minVal;
    private final long maxVal;
    private final long range;

    private BasicHasher hasher = null;

    public AlNumberHash(XKeeper ctx, MskFunc function) {
        String config = function.getText();
        if (config==null)
            config = "";
        final List<String[]> ops = Utils.parseConfig(config);
        if (Utils.hasConfigEntry(ops, "key"))
            this.userKey = ctx.getUserKey(Utils.getConfigValue(ops, "key"));
        else
            this.userKey = null;
        this.minVal = Utils.getConfigLong(ops, "FROM", 0);
        this.maxVal = Utils.getConfigLong(ops, "TO", 10);
        this.range = this.maxVal - this.minVal + 1;
        if (this.range <= 1) {
            throw new AlgoInitException("Bad limits [" + minVal
                + ", " + maxVal + "] for NumberHash algorithm");
        }
    }

    @Override
    public XVector exec(XVector in, XVector out, int iteration) {
        if (in==null || in.values.length==0)
            return null;
        if (hasher==null)
            hasher = new BasicHasher(userKey);
        final long value = hasher.calcHash(in.values, iteration);
        out = XVector.make(out, 1);
        out.values[0] = minVal + ( value % range );
        return out;
    }

    @Override
    public XVector exec(XVector in, XVector out) {
        return exec(in, out, 0);
    }

    @Override
    public boolean isIterationsSupported() {
        return true;
    }

}
