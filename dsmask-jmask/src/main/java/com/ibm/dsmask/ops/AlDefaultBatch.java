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
package com.ibm.dsmask.ops;

import java.util.ArrayList;
import java.util.Set;
import com.ibm.dsmask.impl.*;

/**
 * Default batch algorithm working over a simpler vector algorithm.
 * @author zinal
 */
public class AlDefaultBatch implements XExecutor, StatsDumper {

    private final AlSimpleVector simple;

    public AlDefaultBatch(AlSimpleVector simple) {
        this.simple = simple;
    }

    public AlDefaultBatch(AlSimpleValue simple) {
        this.simple = new AlDefaultVector(simple);
    }

    public boolean isIterationsSupported() {
        return simple.isIterationsSupported();
    }

    /**
     * Compute the values of the algorithm by calling
     * the underlying implementation.
     * This includes the collection of per-row errors.
     * @param ws Execution workspace
     */
    @Override
    public void exec(XWorkspace ws) {
        for (int i=0; i<ws.totalRows; ++i) {
            ws.computeNormal(i, simple);
        }
    }

    /**
     * Perform the necessary number of iterations to ensure that
     * each masked value is unique.
     * This ensures that no two different source values are mapped
     * to a single masked value.
     * @param ws Computational workspace
     */
    public void execUniq(XWorkspace ws) {
        if (ws.step.getUniqCheck() == null) {
            // This should never happen, but just in case...
            exec(ws);
            return;
        }
        // The initial set of indexes is the full one
        final Set<Integer> rowIndexes = ws.buildRowIndexes();
        // We loop until we have an empty set of indexes
        while (!rowIndexes.isEmpty()) {
            // Compute the value for each index
            for ( Integer index : rowIndexes )
                ws.computeIteration(index, simple);
            // Invoke the uniqueness check
            ws.runUniqChecks(rowIndexes);
            // Update the index set as needed
            for ( Integer index : new ArrayList<>(rowIndexes) ) {
                if ( ws.completeUniqCheck(index) )
                    rowIndexes.remove(index);
            }
        }
    }

    @Override
    public void dumpStats(StringBuilder sb) {
        if (simple instanceof StatsDumper)
            ((StatsDumper)simple).dumpStats(sb);
    }

}
