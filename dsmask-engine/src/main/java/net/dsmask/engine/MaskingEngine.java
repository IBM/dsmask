/*
 * Copyright (c) IBM Corp. 2018, 2022.
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
package net.dsmask.engine;

import net.dsmask.model.*;

/**
 * Data masking engine.
 * @author zinal
 */
public class MaskingEngine {

    private final LinkInfo linkInfo;
    private final MaskingProfile profile;
    private final RowContext[] allRows;
    private RowContext pendingRows;
    private int rowCount;

    public MaskingEngine(LinkInfo linkInfo, MaskingProfile profile, int batchSize) {
        batchSize = (batchSize>0) ? batchSize : 1;
        this.linkInfo = linkInfo;
        this.profile = profile;
        this.allRows = new RowContext[batchSize];
        this.pendingRows = null;
        this.rowCount = 0;
    }

    public final int getRowCount() {
        return rowCount;
    }

    /**
     * Reset the engine after row batch processing completion.
     */
    public final void reset() {
        rowCount = 0;
        pendingRows = null;
    }

    /**
     * Add the new row to the input.
     * @param row Input row values
     * @return true, if the row was added, false if the capacity exceeded.
     */
    public boolean addRow(RowInput row) {
        if (rowCount >= allRows.length) {
            // Cannot move forward, batch size exceeded.
            return false;
        }
        RowContext ctx = allRows[rowCount];
        if (ctx==null) {
            // Generate the new row context.
            ctx = new RowContext(linkInfo, profile);
            allRows[rowCount] = ctx;
        }
        // Assign the input row values.
        ctx.setup(row);
        // Mark the row for computation.
        ctx.setNextPending(pendingRows);
        pendingRows = ctx;
        // Increment the number of rows being held.
        ++rowCount;
        return true;
    }

    /**
     * Move forward the computation over the current input set of rows.
     * @return true, if the computation has completed, false otherwise.
     */
    public boolean increment() {
        boolean retval = true;
        RowContext ctx = pendingRows, prev = null;
        while (ctx != null) {
            final RowContext next = ctx.getNextPending();
            if (ctx.increment()) {
                // Exclude the completed row from the list.
                if (prev != null) {
                    prev.setNextPending(next);
                } else {
                    pendingRows = next;
                }
            } else {
                // We have at least one incomplete row.
                retval = false;
            }
            prev = ctx;
            ctx = next;
        }
        return retval;
    }

    /**
     * Collect the output values from the data masking operations.
     * @param pos Row position in the batch list
     * @param row Output row values container.
     */
    public void collect(int pos, RowOutput row) {
        if (pendingRows != null) {
            throw new IllegalStateException("Cannot run collect() with pending rows");
        }
        if (pos >= rowCount) {
            throw new IllegalArgumentException("Row position specified is "
                    + String.valueOf(pos) + ", must be less than " + String.valueOf(rowCount));
        }
        allRows[pos].collect(row);
    }

}
