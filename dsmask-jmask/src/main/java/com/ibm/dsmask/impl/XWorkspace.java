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

import java.util.Arrays;
import java.util.Set;
import com.ibm.dsmask.beans.*;
import com.ibm.dsmask.ops.AlSimpleVector;
import com.ibm.dsmask.ops.AlgoExecException;
import net.dsmask.uniq.UniqRequest;
import net.dsmask.uniq.UniqResponse;

/**
 * Calculation workspace for the current step
 * of the current masking operation.
 * @author zinal
 */
public class XWorkspace {

    public final XKeeper keeper;
    public final XServices services;

    public int totalRows = 0;
    public MskOp operation = null;
    public MskStep step = null;
    public int stepPosition = -1;

    public final XVector[] inputs;
    public final XVector[] outputs;
    public final boolean[] predicates;
    public final XRowErrors[] errors;

    private final UR[] uniqRequest;

    public XWorkspace(XKeeper keeper, XRowState[] rows) {
        this.keeper = keeper;
        this.services = keeper.getServices();
        this.inputs = new XVector[rows.length];
        this.outputs = new XVector[rows.length];
        this.predicates = new boolean[rows.length];
        // workspace has its own references to per-row error contexts
        this.errors = new XRowErrors[rows.length];
        for (int i=0; i<rows.length; ++i)
            this.errors[i] = rows[i].getErrors();
        this.uniqRequest = new UR[rows.length];
    }

    /**
     * Check whether the row needs to be processed in the current step.
     * @param pos Row position, starting from 0.
     * @return true, if row needs to be processed, false otherwise.
     */
    public boolean needProcessRow(int pos) {
        if (pos<0 || pos>=totalRows) {
            throw new IllegalArgumentException("Invalid row position "
                    + pos + ", size " + totalRows);
        }
        return predicates[pos] && (! errors[pos].hasError() );
    }

    /**
     * Clean up the workspace before processing the next batch of rows.
     * @param totalRows Number of rows in the next batch.
     */
    public void nextBatch(int totalRows) {
        if (totalRows > inputs.length)
            throw new IllegalArgumentException();
        this.totalRows = totalRows;
        for (int i=0; i<totalRows; ++i)
            this.errors[i].clear();
    }

    /**
     * Configure the workspace for the next masking operation.
     * @param oper Masking operation to be performed
     */
    public void nextOperation(MskOp oper) {
        this.operation = oper;
        this.step = null;
        this.stepPosition = -1;
    }

    /**
     * Configure the workspace for the next step to be performed.
     * @param step Step of the current masking operation.
     * @param stepPosition Step number within the masking operation.
     */
    public void nextStep(MskStep step, int stepPosition) {
        this.step = step;
        this.stepPosition = stepPosition;
        Arrays.fill(inputs, null);
        Arrays.fill(outputs, null);
        Arrays.fill(predicates, true);
        if (step!=null && step.getUniqCheck()!=null) {
            // prepare for uniq-check
            for (UR ur : uniqRequest) {
                if (ur!=null)
                    ur.clear();
            }
            services.resetIterationNumber();
        }
    }

    /* -------------------------------------------------------- */
    /* Methods below implement the details of uniq-check calls */

    /**
     * Generate the full set of indexes for uniq-check processing.
     * @return Set of integers from 0 to totalRows-1
     */
    public Set<Integer> buildRowIndexes() {
        final Set<Integer> retval = new java.util.TreeSet<>();
        for (int index = 0; index < totalRows; ++index)
            retval.add(index);
        return retval;
    }

    /**
     * Compute the value of expression for the specified index
     * without passing the iteration number.
     * @param index Index to perform computation at
     * @param expression The expression to be computed
     */
    public void computeNormal(int index, AlSimpleVector expression) {
        if (!needProcessRow(index))
            return; // We have either the error, or the false predicate value.
        XVector output = outputs[index], input = inputs[index];
        boolean clearOutput = true;
        // Execute the desired operation, no iterations
        try {
            output = expression.exec(input, output);
            clearOutput = false;
        } catch(AlgoExecException ex) {
            // Collect the execution error
            errors[index].addAlgoError(this, input, ex);
        }
        if (output==null) {
            // No output, even not from the previous iteration.
            // Allocate the vector of the same size as input.
            output = new XVector(input.values.length);
        } else if (clearOutput) {
            // We do not have a usable output.
            // Clear the values from previous iteration.
            output.clear();
        }
        outputs[index] = output;
    }

    /**
     * Compute the value of expression for the specified index
     * at the current iteration number.
     * @param index Index to perform computation at
     * @param expression The expression to be computed
     */
    public void computeIteration(int index, AlSimpleVector expression) {
        if (!needProcessRow(index))
            return; // We have either the error, or the false predicate value.
        XVector output = outputs[index], input = inputs[index];
        boolean clearOutput = true;
        // Grab the request, initialize it if needed.
        UR ur = makeRequest(index);
        // Execute the desired operation, passing the current iteration number
        try {
            output = expression.exec(input, output, ur.getIteration());
            clearOutput = false;
        } catch(AlgoExecException ex) {
            // Collect the execution error
            errors[index].addAlgoError(this, input, ex);
        }
        if (output==null) {
            // No output, even not from the previous iteration.
            // Allocate the vector of the same size as input.
            output = new XVector(input.values.length);
        } else if (clearOutput) {
            // We do not have a usable output.
            // Clear the values from previous iteration.
            output.clear();
        }
        outputs[index] = output;
    }

    private UR makeRequest(int index) {
        UR ur = uniqRequest[index];
        if ( ur == null ) {
            ur = new UR();
            uniqRequest[index] = ur;
        }
        return ur;
    }

    /**
     * Invoke the uniq-check service and process the results.
     * @param rowIndexes Indexes of rows to be processed
     */
    public void runUniqChecks(Set<Integer> rowIndexes) {
        final int[] inputPos = step.getUniqCheck().getInputPositions();
        final int[] outputPos = step.getUniqCheck().getOutputPositions();
        final UR[] requests = new UR[rowIndexes.size()];
        int position = 0;
        for (Integer rowNum : rowIndexes) {
            UR ur = makeRequest(rowNum);
            // update source only when needed
            if (ur.getSource()==null)
                ur.setSource(project(inputs, rowNum, inputPos));
            // always update target
            ur.setTarget(project(outputs, rowNum, outputPos));
            requests[position++] = ur;
        }
        String provider = step.getUniqCheck().getProvider();
        // Invoke the service
        final UniqResponse[] responses = services.getUniqProvider().store(provider, requests);
        // Collect the responses
        for (int pos=0; pos<responses.length; ++pos) {
            requests[pos] . setResponse(responses[pos]);
        }
    }

    private static Object[] project(XVector[] src, int rowNum, int[] colPos) {
        if (colPos == null)
            return src[rowNum].values;
        final Object[] source = src[rowNum].values;
        final Object[] retval = new Object[colPos.length];
        for (int i=0; i<colPos.length; ++i) {
            int colIndex = colPos[i];
            if (colIndex >=0 && colIndex < source.length)
                retval[i] = source[ colIndex ];
        }
        return retval;
    }

    /**
     * Validate the results of uniq-check call and determine the need
     * for the next call.
     * Updates the iteration number, when needed.
     * @param index Row index
     * @return true, of the uniq-check is complete for the row, false otherwise.
     */
    public boolean completeUniqCheck(int index) {
        final UR ur = makeRequest(index);
        final UniqResponse response = ur.getResponse();
        if (response==null) {
            // nothing done yet, need further processing
            return false;
        }
        if (response.isLinkedCorrectly()) {
            // uniq-check service has confirmed the operation
            return  true;
        }
        // update the conflict value, if one was not set
        if (ur.getConflict() == null)
            ur.setConflict(ur.getTarget());
        // check for the hint response
        if (response.getIteration() > 0) {
            // there is a hint, is it usable?
            if (response.getIteration() == ur.getIteration()) {
                // the hint has just failed
                ur.setHintFailed(true);
                // we will have to start from iteration 1.
                ur.setIteration(1);
            } else if (ur.isHintFailed()) {
                // the hint has failed before, so we just move to the next iteration
                ur.nextIteration();
            } else {
                // let's try the hint provided
                ur.setIteration(response.getIteration());
            }
        } else {
            // no hint and negative response, so just move to the next iteration
            ur.nextIteration();
        }
        // if the iteration number is too high, report an error
        if (ur.getIteration() > 50000) {
            errors[index].addOperText(operation, "Failed uniq-check for input ["
                    + ur.getSource() + "], provider [" + step.getUniqCheck().getProvider() + "]");
            return true;
        }
        return false;
    }

    private static class UR extends UniqRequest {
        private UniqResponse response;
        private boolean hintFailed;

        public UR() {
            super();
            this.response = null;
            this.hintFailed = false;
        }

        public UniqResponse getResponse() {
            return response;
        }

        public void setResponse(UniqResponse response) {
            this.response = response;
        }

        public boolean isHintFailed() {
            return hintFailed;
        }

        public void setHintFailed(boolean hintFailed) {
            this.hintFailed = hintFailed;
        }

        /**
         * Advance the iteration counter for unique check.
         */
        public void nextIteration() {
            setIteration(1 + getIteration());
        }

        /**
         * Prepare the object for re-use.
         */
        @Override
        public void clear() {
            this.response = null;
            this.hintFailed = false;
            super.clear();
        }

    }
}
