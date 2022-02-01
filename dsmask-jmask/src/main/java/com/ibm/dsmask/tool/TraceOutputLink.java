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

import java.util.List;
import com.ibm.dsmask.impl.*;

/**
 *
 * @author zinal
 */
public class TraceOutputLink implements XLinkOutput {

    private final XLinkInput input;
    private List<XColumnInfo> columns;

    public TraceOutputLink(XLinkInput input) {
        this.input = input;
    }

    @Override
    public List<XColumnInfo> getOutputColumns() {
        // input and output structure exactly the same
        if (columns == null)
            columns = input.getInputColumns();
        return columns;
    }

    @Override
    public XRowOutput getOutputRecord() {
        return new DummyOutput(getOutputColumns().size());
    }

    @Override
    public void writeRecord(XRowOutput record) {
        final DummyOutput dummy = (DummyOutput) record;
        final StringBuilder sb = new StringBuilder();
        sb.append("OUT-REC");
        for (Object v : dummy.getValues()) {
            sb.append("\t");
            if ( v==null )
                sb.append("<NUL>");
            else
                sb.append(v.toString());
        }
        System.out.println(sb.toString());
    }

    @Override
    public boolean hasRejectLink() {
        return false;
    }

    @Override
    public XRowReject getRejectRecord(XRowInput inputRecord) {
        return null;
    }

    @Override
    public void writeRecord(XRowReject rejectRecord) {
    }

}
