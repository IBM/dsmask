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

import com.ibm.dsmask.impl.*;
import com.ibm.is.cc.javastage.api.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * DataStage adapter to Masker interface - link implementation.
 * @author zinal
 */
public class DsLink implements XLinkInput, XLinkOutput {

    private final InputLink inputLink;
    private final OutputLink outputLink;
    private final OutputLink rejectLink;

    public DsLink(InputLink inputLink, OutputLink outputLink,
            OutputLink rejectLink) {
        this.inputLink = inputLink;
        this.outputLink = outputLink;
        this.rejectLink = rejectLink;
    }

    @Override
    public XRowInput readRecord() {
        InputRecord ir = inputLink.readRecord();
        if (ir==null)
            return null;
        return new DsRowInput(ir);
    }

    @Override
    public XRowOutput getOutputRecord() {
        return new DsRowOutput(outputLink.getOutputRecord());
    }

    @Override
    public void writeRecord(XRowOutput r) {
        if (r==null)
            return;
        outputLink.writeRecord(((DsRowOutput)r).getOutput());
    }

    @Override
    public boolean hasRejectLink() {
        return (rejectLink != null);
    }

    @Override
    public XRowReject getRejectRecord(XRowInput r) {
        if (rejectLink == null)
            return null;
        return new DsRowReject(rejectLink.getRejectRecord(
                ((DsRowInput)r).getInput()));
    }

    @Override
    public void writeRecord(XRowReject r) {
        if (rejectLink==null)
            return;
        rejectLink.writeRecord(((DsRowReject)r).getReject());
    }

    @Override
    public List<XColumnInfo> getInputColumns() {
        if (inputLink==null)
            return Collections.emptyList();
        final List<XColumnInfo> retval = new ArrayList<>();
        for (ColumnMetadata cm : inputLink.getColumnMetadata()) {
            retval.add(new XColumnInfo(cm.getIndex(), cm.getName()));
        }
        return retval;
    }

    @Override
    public List<XColumnInfo> getOutputColumns() {
        if (outputLink==null)
            return Collections.emptyList();
        final List<XColumnInfo> retval = new ArrayList<>();
        for (ColumnMetadata cm : outputLink.getColumnMetadata()) {
            retval.add(new XColumnInfo(cm.getIndex(), cm.getName()));
        }
        return retval;
    }

}
