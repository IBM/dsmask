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

import com.ibm.dsmask.beans.*;

/**
 * Computational state for all masking operations over a single row.
 * It consists of states for each of the masking operations.
 * @author zinal
 */
public class XRowState {

    private final XBulkState bulkState;
    private final XOperState[] opers;
    private XRowInput input = null;
    private XRowOutput output = null;
    private final XRowErrors errors;

    public XRowState(MskTable table, XBulkState bulkState) {
        this.bulkState = bulkState;
        this.opers = new XOperState[table.getOperations().size()];
        this.errors = new XRowErrors();
        int pos = 0;
        for (MskOp op : table.getOperations()) {
            this.opers[pos] = new XOperState(op, this);
            ++pos;
        }
    }

    public XRowErrors getErrors() {
        return errors;
    }

    public final XOperState findOper(MskOp op, int hint) {
        if (hint>=0 && hint<opers.length) {
            XOperState tmp = opers[hint];
            if (tmp.getOperation() == op)
                return tmp;
        }
        // TODO: log warning (hint not working)
        for (XOperState state : opers) {
            if (state.getOperation() == op)
                return state;
        }
        throw new IllegalStateException("Cannot find operation #"
                + op.getId() + " at row position #" + hint);
    }

    /**
     * Remove the data values from the previous run,
     * including the temporary output record.
     */
    public void clear() {
        input = null;
        output = null;
        errors.clear();
        for (XOperState op : opers) {
            op.clear();
        }
    }

    public XRowInput getInputRecord() {
        return input;
    }

    /**
     * Extract necessary values from input record for masking operations.
     * Also builds output row, which is stored inside and used as the target.
     * @param input Input record
     * @param output The corresponding output row
     */
    public void readRow(XRowInput input, XRowOutput output) {
        this.input = input;
        this.output = output;
        for (XOperState oper : opers)
            oper.readRow(input);
    }

    /**
     * Put masked values to the output row.
     * @return Masked output row, to be written to the output link.
     */
    public XRowOutput writeRow() {
        if (!errors.hasError()) {
            // only apply operations if there were no errors
            for (XOperState oper : opers) {
                oper.writeRow(output);
            }
        }
        return output;
    }

    /**
     * @return true, if the record should be rejected, and false otherwise
     */
    public boolean isReject() {
        return errors.hasError();
    }

    /**
     * The callback, invoked from XOperState.writeRow(),
     * to check whether null values are acceptable for the output.
     * Sets the internal failure flag if no.
     * @param outputIndex Index of output NULL column
     * @param operation Masking operation, resulting in setting NULL value
     * @return true, if null is allowed due to input also null, false otherwise
     */
    public boolean checkNull(int outputIndex, MskOp operation) {
        XColumnInfo inCol = bulkState.getIndexMap().get(outputIndex);
        if (inCol==null)
            return true; // strange, but let's skip it...
        Object v = input.getValue(inCol.getIndex());
        if (v==null)
            return true; // okay, that's expected
        // So we have an output NULL value with the corresponding
        // non-NULL input value, and we need to reject the row.
        errors.addOperNull(operation, inCol.getName(), outputIndex, v);
        return false;
    }

    public String formatRejectData() {
        return errors.formatRejectData();
    }

}