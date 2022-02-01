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
package com.ibm.dsmask.impl;

import java.util.Arrays;

/**
 * Vector of values - intermediate structure for data masking algorithms
 * @author zinal
 */
public class XVector {

    public final Object[] values;

    public XVector(int len) {
        this.values = new Object[len];
    }

    public XVector(XVector in) {
        this.values = new Object[in.values.length];
        for (int i=0; i<values.length; ++i)
            this.values[i] = in.values[i];
    }

    public void clear() {
        Arrays.fill(values, null);
    }

    public boolean isAllNull() {
        for (int i=0; i<values.length; ++i) {
            if (values[i]!=null)
                return false;
        }
        return true;
    }

    public Object getValue(int position) {
        if (position>0 && position<=values.length)
            return values[position-1];
        return null;
    }

    public static XVector make(XVector v, int len) {
        if (v==null || v.values.length != len)
            return new XVector(len);
        return v;
    }

    public static XVector inPlaceCopy(XVector v, XVector data) {
        v = make(v, data.values.length);
        for (int i = 0; i<data.values.length; ++i) {
            v.values[i] = data.values[i];
        }
        return v;
    }

    @Override
    public String toString() {
        return Arrays.deepToString(values);
    }

}
