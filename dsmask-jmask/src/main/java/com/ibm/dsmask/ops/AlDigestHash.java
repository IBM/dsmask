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
import com.ibm.dsmask.algo.BasicDigest;

/**
 * Hash value supporting algorithm.
 * Can be used in more complex repeatable hashing algorithms.
 * @author zinal
 */
public class AlDigestHash implements AlSimpleVector {

    private final BasicDigest impl;

    public AlDigestHash(XKeeper ctx, MskFunc function) {
        String config = function.getText();
        if (config==null)
            config = "";
        final String algoName;
        final String userKey;
        final List<String[]> ops = Utils.parseConfig(config);
        if (Utils.hasConfigEntry(ops, "type"))
            algoName = Utils.getConfigValue(ops, "type");
        else
            algoName = null;
        if (Utils.hasConfigEntry(ops, "key"))
            userKey = ctx.getUserKey(Utils.getConfigValue(ops, "key"));
        else
            userKey = null;
        this.impl = new BasicDigest(algoName, userKey);
    }

    @Override
    public XVector exec(XVector in, XVector out, int iteration) {
        if (in==null || in.values.length==0)
            return null;
        byte[] result = impl.calcBinary(in.values, iteration);
        String strResult = BasicDigest.toHex(result);
        out = XVector.make(out, 2);
        out.values[0] = strResult;
        out.values[1] = result;
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
