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
import com.ibm.dsmask.algo.CharClassSet;
import com.ibm.dsmask.algo.MaskFPE;
import com.ibm.dsmask.beans.*;
import com.ibm.dsmask.impl.Utils;
import com.ibm.dsmask.impl.XKeeper;

/**
 * Naive Java-only FPE algorithm implementation.
 * @author mzinal
 */
public class AlFPE implements AlSimpleValue {

    private final MaskFPE algo;

    public AlFPE(XKeeper keeper, MskFunc func) {
        List<String[]> conf = Utils.parseConfig(func.getText());

        CharClassSet charClassSet = keeper.getCharClassSet(
                Utils.getConfigValue(conf, "class"));
        String userKey = keeper.getUserKey(Utils.getConfigValue(conf, "key"));
        int skipBefore = Utils.getConfigInt(conf, "skip-before", 0);
        int skipAfter = Utils.getConfigInt(conf, "skip-after", 0);

        this.algo = new MaskFPE(charClassSet, userKey, skipBefore, skipAfter);
    }

    @Override
    public Object exec(Object in) {
        return algo.calculate(in, 0);
    }

    @Override
    public boolean isIterationsSupported() {
        return true;
    }

    @Override
    public Object exec(Object in, int iteration) {
        return algo.calculate(in, iteration);
    }

}
