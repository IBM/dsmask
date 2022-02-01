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

import com.ibm.dsmask.ops.AlDefaultBatch;
import com.ibm.dsmask.beans.*;

/**
 * Computational states for all rows in the context of a single masking
 * operation.
 * @author zinal
 */
public class XOperRows {

    // Masking operation to be performed
    private final MskOp operation;
    // Operation state, for each row
    private final XOperState[] states;

    public XOperRows(MskOp op, XRowState[] rows, int hint) {
        this.operation = op;
        this.states = new XOperState[rows.length];
        for (int i=0; i<rows.length; ++i) {
            this.states[i] = rows[i].findOper(op, hint);
        }
    }

    public MskOp getOperation() {
        return operation;
    }

    /**
     * Perform masking operation over a batch of rows.
     * The algorithm runs steps sequentially, passing a pack of rows
     * to each step invocation.
     * @param ws Computational workspace
     */
    public void maskRows(XWorkspace ws) {
        int stepPosition = 0;
        for (MskStep step : operation.getRule().getSteps()) {
            // Advance workspace to the next step
            ws.nextStep(step, stepPosition);
            // Prepare input and output vectors.
            //   Output vectors are re-used to decrease re-allocation
            for (int i=0; i<ws.totalRows; ++i) {
                ws.inputs[i] = states[i].buildInput(stepPosition);
                ws.outputs[i] = states[i].getOutput(stepPosition);
            }
            final XExecutor predicate = ws.keeper.makePredicate(step);
            if (predicate != null) {
                // we need to compute the value of per-row predicates
                predicate.exec(ws);
            }
            // Grab the algorithm executor
            final XExecutor executor =
                    ws.keeper.makeExecutor(step.getFunction());
            // Do we have a uniqueness check defined?
            if (step.getUniqCheck() != null) {
                // Run a special version of algorithm with uniqueness
                // checks and re-iterations.
                maskRowsUniq(ws, executor, step);
            } else {
                // Just execute the algorithm
                executor.exec(ws);
            }
            // Write to output vectors
            for (int i=0; i<ws.totalRows; ++i) {
                states[i].setOutput(stepPosition, ws.outputs[i]);
            }
            // Switch to next step position
            ++ stepPosition;
        }
    }

    private void maskRowsUniq(XWorkspace ws, XExecutor executor, MskStep step) {
        if (executor==null)
            throw new IllegalArgumentException();
        // Right now all uniq-checks are supported over AlDefaultBatch.
        if (!(executor instanceof AlDefaultBatch)) {
            throw new IllegalArgumentException("Executor type "
                    + executor.getClass().getSimpleName()
                    + " is not supported for uniq-check");
        }
        AlDefaultBatch batch = (AlDefaultBatch) executor;
        // AlDefaultBatch instance need to support iterations.
        if (! batch.isIterationsSupported()) {
            throw new IllegalArgumentException("Masking function ["
                    + step.getFunctionName()
                    + "] does not support iterations,"
                            + "so it cannot be used by uniq-check");
        }
        // Call the special uniq-check execution sequence.
        batch.execUniq(ws);
    }

}
