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

import com.ibm.nex.odpp.*;
import com.ibm.dsmask.beans.*;
import com.ibm.dsmask.impl.*;

/**
 * Call IBM InfoSphere Optim Data Privacy Providers function.
 * @author zinal
 */
public class AlOptim implements XExecutor {

    private final String config;
    private final ODPP odpp;
    private final NativeProvider provider;

    private RowSet rowset = null;
    private int rowsetSize = 0;

    public AlOptim(XKeeper ctx, MskFunc function) {
        this.config = convertConfig(ctx, function.getText());
        this.odpp = ctx.getServices().getODPP();
        try {
            this.provider = odpp.createProvider(this.config);
        } catch(ODPPRuntimeException rx) {
            throw convertError(rx);
        }
    }

    private RuntimeException convertError(ODPPRuntimeException rx) {
        final String eol = System.getProperty("line.separator");
        final StringBuilder sb = new StringBuilder();
        sb.append("ODPP error for config [")
                .append(config).append("]");
        if (rx.getMessage()!=null)
            sb.append(eol).append(rx.getMessage());
        for (String msg : rx.getMessages()) {
            sb.append(eol).append(msg);
        }
        return new AlgoInitException(sb.toString());
    }

    private String convertConfig(XKeeper keeper, String config) {
        return keeper.injectKeys(config) + ",flddef1=(name=f1,dt=WVARCHAR_SZ)";
    }

    @Override
    public void exec(XWorkspace ws) {
        // the number of columns is the same for all rows in a batch
        final int ncolumns = ws.inputs[0].values.length;
        // count the number of non-null values
        final int totalCount = countValues(ws);
        int position;
        if (totalCount > 0) {
            // allocate rowset of necessary size,
            //   avoiding unnecessary reallocation
            if (rowset==null || totalCount!=rowsetSize) {
                rowset = odpp.createRowSet(totalCount,
                        new FieldDescriptor[] {
                            new FieldDescriptor("f1", provider)
                        });
                rowsetSize = totalCount;
            }
            // prepare the input by putting the values into the rowset
            position = 0;
            for (int i=0; i<ws.totalRows; ++i) {
                if (ws.needProcessRow(i)) {
                    for (int j=0; j<ncolumns; ++j) {
                        final Object sourceValue = ws.inputs[i].values[j];
                        if (sourceValue != null) {
                            // copy all non-null values from not skipped rows
                            rowset.getRow(position).getField(0).setValue(
                                    sourceValue.toString()
                            );
                            ++position;
                        }
                    }
                }
            }
            // ODPP call
            try {
                provider.service(rowset);
            } catch(ODPPRuntimeException rx) {
                throw convertError(rx);
            }
        }
        // extract the output values
        position = 0;
        for (int i=0; i<ws.totalRows; ++i) {
            final XVector input = ws.inputs[i],
                    // re-allocate output, if necessary
                    output = XVector.make(ws.outputs[i], ncolumns);
            if (ws.needProcessRow(i)) {
                // copy column values from rowset, for non-null inputs
                for (int j=0; j<ncolumns; ++j) {
                    final Object sourceValue = input.values[j];
                    if (sourceValue != null) {
                        output.values[j] =
                                rowset.getRow(position).getField(0).getValue();
                        ++position;
                    } else {
                        output.values[j] = null;
                    }
                }
            } else {
                // clear the values from previous iteration
                output.clear();
            }
            // necessary, as output may have been re-allocated
            ws.outputs[i] = output;
        }
    }

    /**
     * Count the number of non-null values in the input records
     * @param ws workspace
     * @return total number of non-null values
     */
    private int countValues(XWorkspace ws) {
        if (ws.totalRows==0)
            return 0;
        int numValues = 0;
        final int numColumns = ws.inputs[0].values.length;
        for (int i=0; i<ws.totalRows; ++i) {
            if (ws.needProcessRow(i)) {
                for (int j=0; j<numColumns; ++j) {
                    if (ws.inputs[i].values[j] != null) {
                        ++numValues;
                    }
                }
            }
        }
        return numValues;
    }

}
