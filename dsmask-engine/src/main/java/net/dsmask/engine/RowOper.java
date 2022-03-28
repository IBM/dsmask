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

import java.util.List;
import java.util.Map;
import net.dsmask.model.*;

/**
 * Masking operation context for a single row.
 * @author zinal
 */
public class RowOper {

    private final CtxItemBlock context;
    private final int[] indexInput;
    private final int[] indexOutput;

    public RowOper(LinkInfo linkInfo, MaskingProfile prof, MaskingOperation op) {
        this.context = new CtxItemBlock(op.getRule());
        this.indexInput = index(linkInfo.getInputLink().getInputColumns(),
                linkInfo.getInputMap(), op.getInputs());
        this.indexOutput = index(linkInfo.getOutputLink().getOutputColumns(),
                linkInfo.getOutputMap(), op.getOutputs());
    }

    /**
     * Read the input values from the input row.
     * @param row Input row.
     */
    public void setup(RowInput row) {

    }

    /**
     * Write the output values to the output row.
     * @param row Output row values holder.
     */
    public void collect(RowOutput row) {
        
    }

    /**
     * Move forward the computation over the current masking operation over the current row.
     * @return true, if the computation has completed, false otherwise.
     */
    public boolean increment() {
        return true;
    }

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

    private static int linearSearch(List<XColumnInfo> cols, String name) {
        for (XColumnInfo ci : cols) {
            if (name.equalsIgnoreCase(ci.getName()))
                return ci.getIndex();
        }
        throw new IllegalArgumentException("Missing column " + name
                + " among " + cols.toString());
    }
}
