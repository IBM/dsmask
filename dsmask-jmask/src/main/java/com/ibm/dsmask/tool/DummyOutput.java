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

import com.ibm.dsmask.impl.*;

/**
 *
 * @author zinal
 */
public class DummyOutput implements XRowOutput {

    private final Object[] values;

    public DummyOutput(int count) {
        this.values = new Object[count];
    }

    public Object[] getValues() {
        return values;
    }

    @Override
    public void setValue(int index, Object value) {
        values[index - 1] = value;
    }

    @Override
    public void setValueAsString(int index, String value) {
        values[index - 1] = value;
    }

}
