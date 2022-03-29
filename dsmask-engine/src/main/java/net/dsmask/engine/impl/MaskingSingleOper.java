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
package net.dsmask.engine.impl;

import java.util.List;
import java.util.Map;
import java.util.ArrayDeque;
import net.dsmask.engine.*;
import net.dsmask.model.*;

/**
 * Masking operation context for a single row.
 * @author zinal
 */
public class MaskingSingleOper {

    private final CtxRoot input;
    private final CtxBlock context;
    private final int[] indexInput;
    private final int[] indexOutput;

    private final ArrayDeque<CtxBlock> stack;

    public MaskingSingleOper(Workspace workspace, MaskingOperation op) {
        this.input = new CtxRoot(op.getInputs().size());
        this.context = new CtxBlock(op.getRule());
        this.indexInput = index(workspace.getInputColumns(),
                workspace.getInputMap(), op.getInputs());
        this.indexOutput = index(workspace.getOutputColumns(),
                workspace.getOutputMap(), op.getOutputs());
        this.stack = new ArrayDeque<>();
    }

    /**
     * Read the input values from the input row, and prepare for masking.
     * @param row Input row.
     */
    public final void setup(RowInput row) {
        input.setup(indexInput, row);
        context.setup();
        stack.clear();
        stack.push(context);
    }

    /**
     * Write the output values to the output row.
     * @param row Output row values holder.
     */
    public final void collect(RowOutput row) {
        if (! stack.isEmpty())
            throw new IllegalStateException();
        context.collect(indexOutput, row);
    }

    /**
     * Move forward the computation of the current masking operation on the current row.
     * @return true, if the computation has completed, false otherwise.
     */
    public boolean increment() {
        while (! stack.isEmpty()) {
            final CtxBlock cur = stack.getLast();
            if (cur.getPosition() >= cur.getItems().length) {
                // End of block, so it can be removed from the stack
                stack.removeLast();
            } else {
                // Process next block element
                final int position = cur.getPosition();
                final CtxBase next = cur.getItems()[position];
                switch (next.getItemType()) {
                    case Block:
                    case Fragment:
                        // Adding the block to the stack to be processed on next iteration.
                        stack.addLast((CtxBlock) next);
                        break;
                    case Script:
                        ((CtxScript)next).calculate();
                        break;
                    case Step:
                        if (! ((CtxStep)next).calculate() ) {
                            // Step has been added to the batch.
                            // The execution will have to be resumed after the batch completion.
                            return false;
                        }
                        break;
                }
                cur.setPosition(position + 1);
            }
        }
        return true;
    }

    /**
     * Prepare the array of indexes to access the referenced columns.
     * @param cols Full list of columns in a table
     * @param ixMap Map of column names to column indexes
     * @param refs List of references to columns by name
     * @return Array of column indexes in the same order as defined by refs argument
     */
    private static int[] index(List<XColumnInfo> cols, Map<String, Integer> ixMap,
            List<AnyField> refs) {
        final int[] retval = new int[refs.size()];
        for (int i=0; i<retval.length; ++i) {
            final String name = refs.get(i).getName();
            Integer ix = ixMap.get(name);
            if (ix!=null) {
                retval[i] = ix;
            } else {
                retval[i] = linearSearch(cols, name);
            }
        }
        return retval;
    }

    /**
     * Helper function to get the column index by column name
     * @param cols Full list of columns in a table
     * @param name Column name
     * @return Column index
     */
    private static int linearSearch(List<XColumnInfo> cols, String name) {
        for (XColumnInfo ci : cols) {
            if (name.equalsIgnoreCase(ci.getName()))
                return ci.getIndex();
        }
        throw new IllegalArgumentException("Missing column " + name
                + " among " + cols.toString());
    }
}
