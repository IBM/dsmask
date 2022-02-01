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
package com.ibm.dsmask.tool;

import com.ibm.dsmask.impl.XRowInput;

/**
 *
 * @author zinal
 */
public class DummyInput implements XRowInput {

    public Object[] values;

    public DummyInput() {
    }

    public DummyInput(Object[] values) {
        this.values = values;
    }

    public Object[] getValues() {
        return values;
    }

    public void setValues(Object[] values) {
        this.values = values;
    }

    @Override
    public Object getValue(int index) {
        if (values==null)
            return null;
        if (index < 1 || index > values.length)
            return null;
        return values[index - 1];
    }

}
