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

/**
 *
 * @author zinal
 */
public class AlConcat implements AlSimpleVector {

    private final String separator;

    public AlConcat(MskFunc function) {
        String config = function.getText();
        if (config==null)
            config = "";
        if (config.trim().length()==0) {
            this.separator = " ";
        } else {
            final List<String[]> data = Utils.parseConfig(config);
            this.separator = Utils.getConfigValue(data, "separator");
        }
    }

    @Override
    public XVector exec(XVector in, XVector out) {
        out = XVector.make(out, 1);
        final StringBuilder sb = new StringBuilder();
        boolean allNulls = true;
        boolean sep = false;
        for (int i=0; i<in.values.length; ++i) {
            if (in.values[i]==null)
                continue;
            if (sep) sb.append(separator); else sep = true;
            sb.append(in.values[i].toString());
            allNulls = false;
        }
        if (allNulls) {
            out.values[0] = null;
        } else {
            out.values[0] = sb.toString();
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
