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
import com.ibm.dsmask.beans.MskFunc;

/**
 * Project operator.
 * Copy the specified inputs to outputs
 * @author zinal
 */
public class AlProject implements AlSimpleVector {

    public AlProject(MskFunc function) {
    }

    @Override
    public XVector exec(XVector in, XVector out) {
        if (in==null)
            return null;
        out = XVector.make(out, in.values.length);
        for (int i=0; i<in.values.length; ++i)
            out.values[i] = in.values[i];
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
