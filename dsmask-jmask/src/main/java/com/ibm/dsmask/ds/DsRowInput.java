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
package com.ibm.dsmask.ds;

import com.ibm.dsmask.impl.XRowInput;
import com.ibm.is.cc.javastage.api.InputRecord;

/**
 * Adapter for DataStage input and output records
 * to the masking library interfaces.
 * @author zinal
 */
public class DsRowInput implements XRowInput {

    private final InputRecord input;

    public DsRowInput(InputRecord input) {
        this.input = input;
    }

    public InputRecord getInput() {
        return input;
    }

    @Override
    public Object getValue(int index) {
        return input.getValue(index);
    }

}
