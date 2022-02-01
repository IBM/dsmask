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

import java.math.BigDecimal;
import com.ibm.dsmask.beans.*;

/**
 * Computational state for a single masking operation over a single row of data.
 * It consists of data vectors for all steps that are already processed.
 * @author zinal
 */
public class XOperState {

    private final MskOp operation;
    private final XRowState rowState;
    private final XVector base;
    private final XVector[] inputs;
    private final XVector[] outputs;

    public XOperState(MskOp op, XRowState rowState) {
        this.operation = op;
        this.rowState = rowState;
        final int numSteps = op.getRule().getSteps().size();
        this.base = new XVector(op.getInputFields().size());
        this.inputs = new XVector[numSteps];
        this.outputs = new XVector[numSteps];
    }

    public final MskOp getOperation() {
        return operation;
    }

    /**
     * Remove the data values from previous run.
     */
    public void clear() {
        base.clear();
        for (XVector v : inputs) {
            if (v!=null)
                v.clear();
        }
        for (XVector v : outputs) {
            if (v!=null)
                v.clear();
        }
    }

    /**
     * Extract values from an input row
     * @param record Input record
     */
    public void readRow(XRowInput record) {
        int pos = 0;
        for (int index : operation.getInputIndexes()) {
            // MAYBE: handle data type conversion
            base.values[pos] = record.getValue(index);
            ++pos;
        }
    }

    /**
     * Put values to an output row
     * @param record Output record
     */
    public void writeRow(XRowOutput record) {
        final XVector vec = outputs[outputs.length - 1];
        if (vec==null) {
            throw new IllegalStateException("No row to be written, "
                    + "illegal sequence of calls");
        }
        int pos = 0;
        for (int index : operation.getOutputIndexes()) {
            Object value = vec.values[pos];
            // Validate the output value.
            if (rowState.checkValue(index, operation, value)) {
                // If the value is allowed, we set it to the output.
                if (value==null) {
                    record.setValue(index, null);
                } else {
                    // Minimal handling of data type conversion
                    if (value instanceof String ||
                            value instanceof Integer ||
                            value instanceof Long) {
                        record.setValueAsString(index, value.toString());
                    } else if (value instanceof Double) {
                        record.setValueAsString(index,
                                BigDecimal.valueOf((Double)value).toString());
                    } else {
                        record.setValue(index, value);
                    }
                }
            }
            ++pos;
        }
    }

    /**
     * Get the input vector for step at the specified position.
     * All previous steps should have been executed.
     * @param position Step position
     * @return Refreshed input vector
     */
    public XVector buildInput(int position) {
        if (position<0 || position>=inputs.length) {
            throw new IllegalArgumentException("Illegal input step position "
                + position + " for operation #" + operation.getId());
        }
        MskStep step = operation.getStep(position);
        final XVector vec;
        if (step.getRefs().isEmpty()) {
            // no input defition for the step, take the previous values
            XVector prev = null;
            int cur = 1;
            while (prev == null) {
                prev = getOutput(position - cur);
                ++cur;
            }
            // Copy the previous values as-is
            vec = XVector.make(inputs[position], prev.values.length);
            System.arraycopy(prev.values, 0, vec.values, 0, prev.values.length);
        } else {
            // step has its own input definition
            vec = XVector.make(inputs[position], step.getRefs().size());
            int index = 0;
            for (MskRef ref : step.getRefs()) {
                vec.values[index] = getValue(ref);
                ++index;
            }
        }
        // Store filled vector for re-use
        inputs[position] = vec;
        return vec;
    }

    /**
     * Retrieve the value by reference
     * @param ref Reference
     * @return Value, or null if one is not available
     */
    public Object getValue(MskRef ref) {
        final XVector output = getOutput(ref.getParentIndex());
        if (output == null)
            return null;
        return output.getValue(ref.getPosition());
    }

    /**
     * Get current output vector at the specified position.
     * @param position Step position
     * @return Output vector (may be null, if not previously set)
     */
    public XVector getOutput(int position) {
        if (position<0)
            return base;
        if (position<outputs.length)
            return outputs[position];
        throw new IllegalArgumentException("Illegal output step position "
                + position + " for operation #" + operation.getId());
    }

    /**
     * Store output vector at the specified position.
     * @param position Step position
     * @param vec Output vector
     */
    public void setOutput(int position, XVector vec) {
        if (position < 0 || position>=outputs.length) {
            throw new IllegalArgumentException("Illegal output step position "
                    + position + " for operation #" + operation.getId());
        }
        outputs[position] = vec;
    }

}
