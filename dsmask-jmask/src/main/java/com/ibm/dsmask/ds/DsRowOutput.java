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

import com.ibm.dsmask.impl.XRowOutput;
import com.ibm.is.cc.javastage.api.OutputRecord;

/**
 * DataStage adapter to Masker interface - output row implementation.
 * @author zinal
 */
public class DsRowOutput implements XRowOutput {

    private final OutputRecord output;

    public DsRowOutput(OutputRecord output) {
        this.output = output;
    }

    public OutputRecord getOutput() {
        return output;
    }

    @Override
    public void setValue(int index, Object value) {
        output.setValue(index, value);
    }

    @Override
    public void setValueAsString(int index, String value) {
        output.setValueAsString(index, value);
    }

}
