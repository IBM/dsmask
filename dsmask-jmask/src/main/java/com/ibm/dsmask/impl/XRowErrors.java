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

import java.util.ArrayList;
import java.util.List;
import com.ibm.dsmask.beans.MskOp;

/**
 *
 * @author zinal
 */
public class XRowErrors {

    private final List<String> messages = new ArrayList<>();

    public void clear() {
        messages.clear();
    }

    public boolean hasError() {
        return ! messages.isEmpty();
    }

    public void addText(CharSequence cs) {
        messages.add(cs.toString());
    }

    public void addOperText(MskOp op, String info) {
        final StringBuilder sb = new StringBuilder();
        sb.append("Row in table [").append(op.getTable().getTableInfo())
                .append("], rule [").append(op.getRule().getName())
                .append("] failed with error:");
        sb.append(Utils.EOL).append("\t").append(info);
        addText(sb);
    }

    public void addOperNull(MskOp op, String fieldName,
            int position, Object originalValue) {
        if (originalValue==null)
            originalValue = "?";
        final StringBuilder sb = new StringBuilder();
        sb.append("Row in table [").append(op.getTable().getTableInfo())
                .append("], rule [").append(op.getRule().getName())
                .append("] failed with error:");
        sb.append(Utils.EOL).append("\t")
                .append("Illegal NULL output for column[")
                .append(fieldName).append("] at @").append(position)
                .append(" for original value [")
                .append(originalValue.toString()).append("]");
        addText(sb);
    }

    public void addOperUnmodified(MskOp op, String fieldName,
            int position, Object originalValue) {
        if (originalValue==null)
            originalValue = "?";
        final StringBuilder sb = new StringBuilder();
        sb.append("Row in table [").append(op.getTable().getTableInfo())
                .append("], rule [").append(op.getRule().getName())
                .append("] failed with error:");
        sb.append(Utils.EOL).append("\t")
                .append("Unmodified output for column[")
                .append(fieldName).append("] at @").append(position)
                .append(" for original value [")
                .append(originalValue.toString()).append("]");
        addText(sb);
    }

    public void addAlgoError(XWorkspace ws, XVector input, Throwable ex) {
        if (ex==null) return;
        final StringBuilder sb = new StringBuilder();
        sb.append("Algorithmical error on table [")
                .append(ws.operation.getTable().getTableInfo())
                .append("], rule [")
                .append(ws.operation.getRule().getName())
                .append("], step #").append(ws.stepPosition + 1)
                .append(", function [")
                .append(ws.step.getFunction().getName())
                .append("]");
        if (input!=null) {
            sb.append(Utils.EOL).append("\t");
            sb.append("Input data: ").append(input.toString());
        }
        while (ex!=null) {
            sb.append(Utils.EOL).append("\t");
            sb.append(ex.getClass().getName());
            sb.append(": ");
            sb.append(ex.getMessage());
            ex = ex.getCause();
        }
        this.addText(sb);
    }

    public String formatRejectData() {
        final StringBuilder sb = new StringBuilder();
        boolean delimiter = false;
        for (String cur : messages) {
            if (delimiter) sb.append(Utils.EOL); else delimiter = true;
            sb.append(cur);
        }
        return sb.toString();
    }
}
