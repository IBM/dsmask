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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.ibm.dsmask.beans.*;
import com.ibm.dsmask.SafeLogger;

/**
 * Computational state for a set of masking operations over a bulk of rows.
 * @author zinal
 */
public class XBulkState {

    // per-row data and current number of rows
    private final XRowState[] rows;
    private int totalRows = 0;
    // rows of context data for each operation
    private final List<XOperRows> operRows = new ArrayList<>();
    // calculation workspace
    private final XWorkspace workspace;
    // map output index to corresponding input column
    private Map<Integer, XColumnInfo> indexMap;
    // flag to control whether rejection has been logged
    private boolean rejectionLogged = false;

    public XBulkState(XKeeper ctx, int maxRows) {
        // allocate main row table
        this.rows = new XRowState[maxRows];
        // fill main and per-operation row tables
        for (int pos = 0; pos < maxRows; ++pos) {
            this.rows[pos] = new XRowState(ctx.getTable(), this);
        }
        // allocate and fill per-operation row tables
        int opPos = 0;
        for (MskOp op : ctx.getTable().getOperations()) {
            operRows.add(new XOperRows(op, rows, opPos));
            ++opPos;
        }
        this.workspace = new XWorkspace(ctx, this.rows);
    }

    public Map<Integer, XColumnInfo> getIndexMap() {
        return indexMap;
    }

    public void setIndexMap(Map<Integer, XColumnInfo> indexMap) {
        if (indexMap==null || indexMap.isEmpty())
            throw new IllegalArgumentException();
        this.indexMap = indexMap;
    }

    /**
     * Clear the bulk state, removing all the data values.
     */
    public void clear() {
        totalRows = 0;
        for (XRowState row : rows) {
            row.clear();
        }
    }

    /**
     * Copy values from input to output on indexes specified by a map
     * @param input Input row
     * @param output Output row
     * @param indexMap Map of indexes
     */
    public static void copyValues(XRowInput input, XRowOutput output,
            Map<Integer, XColumnInfo> indexMap) {
        for (Map.Entry<Integer, XColumnInfo> me : indexMap.entrySet()) {
            output.setValue(me.getKey(),
                    input.getValue(me.getValue().getIndex()));
        }
    }

    /**
     * Read a batch of rows from the input link.
     * Prepare the output records to be written after masking.
     * @param inputLink Input link
     * @param outputLink Output link, to generate the output record
     * @return true, if there are rows to be masked and written,
     *          false otherwise.
     */
    public boolean readRows(XLinkInput inputLink, XLinkOutput outputLink) {
        if (totalRows >= rows.length)
            return true; // need to process before reading
        while (totalRows < rows.length) {
            XRowInput input = inputLink.readRecord();
            if (input==null)
                return (totalRows > 0);
            // Manually copy input to output, as a workaround with
            //   DECIMAL and outputLink.getOutputRecord(input) error.
            // (the above is a former story with direct DataStage APIs).
            XRowOutput output = outputLink.getOutputRecord();
            copyValues(input, output, indexMap);
            // Assign input values for operations
            rows[totalRows].readRow(input, output);
            ++totalRows;
        }
        return true;
    }

    /**
     * Fill in and write the (masked) output records.
     * @param outputLink Output link
     */
    public void writeRows(XLinkOutput outputLink) {
        for (int i=0; i<totalRows; ++i) {
            XRowOutput record = rows[i].writeRow();
            if (rows[i].isReject()) {
                final String rejectText = rows[i].formatRejectData();
                if (rejectionLogged==false) {
                    SafeLogger.warning("Record(s) rejected, "
                            + "check input data and masking rules."
                            + Utils.EOL + rejectText);
                    rejectionLogged = true;
                }
                if (outputLink.hasRejectLink()) {
                    XRowReject rejectRecord = outputLink
                            .getRejectRecord(rows[i].getInputRecord());
                    rejectRecord.setErrorCode(1);
                    rejectRecord.setErrorText(rejectText);
                    outputLink.writeRecord(rejectRecord);
                }
            } else {
                outputLink.writeRecord(record);
            }
        }
    }

    /**
     * Perform masking operations over input values and prepare the output.
     */
    public void maskRows() {
        workspace.nextBatch(totalRows);
        for (XOperRows oper : operRows) {
            try {
                workspace.nextOperation(oper.getOperation());
                oper.maskRows(workspace);
            } catch(Exception ex) {
                final StringBuilder sb = new StringBuilder();
                sb.append("Masking operation #")
                        .append(oper.getOperation().getId())
                        .append(" for rule [")
                        .append(oper.getOperation().getRule().getName())
                        .append("] failed");
                throw new RuntimeException(sb.toString(), ex);
            }
        }
    }

}
