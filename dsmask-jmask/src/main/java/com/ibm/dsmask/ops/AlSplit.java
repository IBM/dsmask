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
import java.util.Arrays;
import java.util.regex.Pattern;
import com.ibm.dsmask.impl.*;
import com.ibm.dsmask.beans.MskFunc;

/**
 *
 * @author zinal
 */
public class AlSplit implements AlSimpleVector {

    private final Pattern splitPattern;

    public AlSplit(MskFunc function) {
        String config = function.getText();
        if (config==null)
            config = "";
        if (config.trim().length()==0) {
            this.splitPattern = Pattern.compile("[\\s\r\n]");
        } else {
            final List<String[]> data = Utils.parseConfig(config);
            String pattern = Utils.getConfigValue(data, "rx");
            this.splitPattern = Pattern.compile(pattern);
        }
    }

    @Override
    public XVector exec(XVector in, XVector out) {
        if (in.values.length!=1) {
            throw new IllegalArgumentException("Split algorithm accepts "
                    + "only a single column, got " + in.values.length);
        }
        if (in.values[0]==null) {
            if (out!=null) {
                Arrays.fill(out.values, null);
            } else {
                out = XVector.make(out, 1);
            }
        } else {
            String[] temp = splitPattern.split(in.values[0].toString());
            out = XVector.make(out, temp.length);
            System.arraycopy(temp, 0, out.values, 0, temp.length);
        }
        return out;
    }

    @Override
    public boolean isIterationsSupported() {
        return false;
    }

    @Override
    public XVector exec(XVector in, XVector out, int iteration) {
        return exec(in, out);
    }

}
