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
            // Only apply futher operations if there were no errors.
            // Otherwise the older error should be reported to the reject link.
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
     * to check whether the masked value acceptable.
     * Sets the internal failure flag if no.
     * @param outputIndex Index of output NULL column
     * @param operation Masking operation, resulting in setting NULL value
     * @param value
     * @return true, if the value is acceptable, false otherwise
     */
    public boolean checkValue(int outputIndex, MskOp operation, Object value) {
        XColumnInfo inCol = bulkState.getIndexMap().get(outputIndex);
        if (inCol==null) {
            // strange, but let's skip it...
            return true;
        }
        Object original = input.getValue(inCol.getIndex());
        if (original==null) {
            // if the input was null, we allow any output
            return true;
        }
        if (value==null) {
            // We have an output NULL value with the corresponding
            // non-NULL input value, and we need to reject the row.
            errors.addOperNull(operation, inCol.getName(), outputIndex, original);
            return false;
        }
        /*
        Disabled the output same-value checks, as this is a valid situation
        for many real scenarious. Needs to be done depending on the actual
        masking operation characteristics - which is a TODO item.

        if (operation.getOutputIndexes().length == 1) {
            // Value validation is performed only for single-value masking operations.
            String temp1 = original.toString().trim();
            String temp2 = value.toString().trim();
            if (temp1.equalsIgnoreCase(temp2)) {
                // Input equals output - bad masking.
                errors.addOperUnmodified(operation, inCol.getName(), outputIndex, original);
                return false;
            }
        }
        */
        return true;
    }

    public String formatRejectData() {
        return errors.formatRejectData();
    }

}
