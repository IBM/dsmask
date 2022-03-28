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

import java.util.ArrayList;
import java.util.List;
import net.dsmask.model.*;

/**
 * Masking context for a single row.
 * @author zinal
 */
public class RowContext {

    private final List<RowOper> operations;

    private RowContext nextPending = null;

    public RowContext(Workspace workspace) {
        this.operations = new ArrayList<>(workspace.getOperations().size());
        for (MaskingOperation op : workspace.getOperations()) {
            this.operations.add(new RowOper(workspace, op));
        }
    }

    public final RowContext getNextPending() {
        return nextPending;
    }

    public final void setNextPending(RowContext nextPending) {
        this.nextPending = nextPending;
    }

    /**
     * Reset the internal state after the computation has completed.
     */
    public final void reset() {
        nextPending = null;
    }

    /**
     * Clear the state, set the input values for all operations.
     * @param row Input row values
     */
    public final void setup(RowInput row) {
        for (RowOper ro : operations) {
            ro.setup(row);
        }
    }

    /**
     * Grab the outputs from all the operations, merge into the output row.
     * @param link Output link to send the row
     */
    public final void collect(LinkOutput link) {
        for (RowOper ro : operations) {
            //ro.collect(row);
        }
    }

    /**
     * Move forward the computation over the current row.
     * @return true, if the computation has completed, false otherwise.
     */
    public final boolean increment() {
        boolean retval = true;
        for (RowOper ro : operations) {
            final boolean operComplete = ro.increment();
            if (!operComplete)
                retval = false;
        }
        return retval;
    }

}
