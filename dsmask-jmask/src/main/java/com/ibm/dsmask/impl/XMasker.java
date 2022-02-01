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

import java.util.HashMap;
import java.util.Map;
import com.ibm.dsmask.beans.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Top-level masking process implementation.
 * @author zinal
 */
public class XMasker implements Runnable {

    // maps output column index -> input column metadata
    //  (including index and name)
    private final Map<Integer, XColumnInfo> indexMap = new HashMap<>();
    // input records source
    private XLinkInput input;
    // output records destination
    private XLinkOutput output;
    // batch size
    private int batchSize = 100;

    // masking configuration
    private MskContext context = null;
    // masking services
    private XServices services = null;

    // missing columns
    private List<String> missingColumns = null;
    // performance statistics
    private StringBuilder perfStats = null;

    public XMasker() {
        this.input = null;
        this.output = null;
    }

    public XMasker(XLinkInput input, XLinkOutput output) {
        this.input = input;
        this.output = output;
    }

    public XMasker(XLinkInput input, XLinkOutput output, int batchSize) {
        this.input = input;
        this.output = output;
        this.batchSize = (batchSize>0) ? batchSize : 1;
    }

    public XLinkInput getInput() {
        return input;
    }

    public void setInput(XLinkInput input) {
        this.input = input;
    }

    public XLinkOutput getOutput() {
        return output;
    }

    public void setOutput(XLinkOutput output) {
        this.output = output;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(int batchSize) {
        if (batchSize < 1)
            this.batchSize = 1;
        else
            this.batchSize = batchSize;
    }

    public MskContext getContext() {
        return context;
    }

    public void setContext(MskContext context) {
        this.context = context;
    }

    public XServices getServices() {
        return services;
    }

    public void setServices(XServices services) {
        this.services = services;
    }

    public List<String> getMissingColumns() {
        if (missingColumns==null)
            return Collections.emptyList();
        return missingColumns;
    }

    public StringBuilder getPerfStats() {
        return perfStats;
    }

    @Override
    public void run() {
        if (indexMap.isEmpty() || input==null || output==null)
            throw new IllegalStateException();
        if (context==null) {
            // No masking context. Just copy records from input to output.
            while (true) {
                XRowInput ir = input.readRecord();
                if (ir==null)
                    break; // no more input
                XRowOutput or = output.getOutputRecord();
                XBulkState.copyValues(ir, or, indexMap);
                output.writeRecord(or);
            }
        } else {
            // We have a masking context and need to apply masking operations.
            if (services==null)
                services = new XServices();
            try (XKeeper keeper = new XKeeper(context, services)) {
                final XBulkState bulkState = new XBulkState(keeper, batchSize);
                bulkState.setIndexMap(indexMap);
                while (bulkState.readRows(input, output)) {
                    bulkState.maskRows();
                    bulkState.writeRows(output);
                    bulkState.clear();
                }
                dumpStatistics(keeper);
            } finally {
                services.close();
            }
        }
    }

    private void dumpStatistics(XKeeper keeper) {
        perfStats = null;
        final StringBuilder sb = new StringBuilder();
        keeper.dumpStats(sb);
        if (sb.length() > 0) {
            perfStats = sb;
        }
    }

    /**
     * Generate an index map for later use.
     * In case some columns are missing, poplulate the list of missing
     * column names for diagnostic.
     * @return true, if all necessary columns were mapped, false otherwise.
     */
    public boolean buildIndexMap() {
        // index map is necessary to properly construct output records
        indexMap.clear();
        final List<XColumnInfo> inputColumns = input.getInputColumns();
        final List<XColumnInfo> outputColumns = output.getOutputColumns();
        for (XColumnInfo out : outputColumns) {
            int position = out.getIndex() - 1;
            if (position >=0 && position < inputColumns.size()) {
                // try to find the corresponding column by position
                XColumnInfo inLucky = inputColumns.get(position);
                if (inLucky!=null && inLucky.getName()
                        .equalsIgnoreCase(out.getName())) {
                    indexMap.put(out.getIndex(), inLucky);
                    continue;
                }
            }
            // if the positions are meshed, look up by column names
            boolean found = false;
            for (XColumnInfo in : inputColumns) {
                if (in.getName().equalsIgnoreCase(out.getName())) {
                    indexMap.put(out.getIndex(), in);
                    found = true;
                    break;
                }
            }
            if (!found) {
                // no column? no work!
                if (missingColumns==null)
                    missingColumns = new ArrayList<>();
                missingColumns.add(out.getName());
            }
        }
        return (missingColumns==null) || missingColumns.isEmpty();
    }

    /**
     * Check that all fields required for masking operations exist.
     * Build indexes (fields positions) for masking operations.
     * @throws Exception
     */
    public void validateFields() throws Exception {
        if (context==null)
            return;
        final List<XColumnInfo> inputColumns = input.getInputColumns();
        final List<XColumnInfo> outputColumns = output.getOutputColumns();
        for (MskOp op : context.getTable().getOperations()) {
            // allocate indexes
            final int inputIndexes[] = new int[op.getInputFields().size()];
            final int outputIndexes[] = new int[op.getOutputFields().size()];
            // check and index input fields
            indexFields(inputIndexes, inputColumns, op.getInputFields());
            // check and index output fields
            indexFields(outputIndexes, outputColumns, op.getOutputFields());
            // save indexes for use during masking
            op.setInputIndexes(inputIndexes);
            op.setOutputIndexes(outputIndexes);
        }
    }

    public void indexFields(int[] inputIndexes, List<XColumnInfo> cmlist,
            List<String> fields) throws Exception {
        for (int i=0; i<inputIndexes.length; ++i) {
            String fieldName = fields.get(i);
            XColumnInfo cur = findColumn(cmlist, fieldName);
            if (cur==null) {
                throw new Exception("Missing field [" + fieldName + "] in "
                        + context.getTableInfo());
            }
            inputIndexes[i] = cur.getIndex();
        }
    }

    public static XColumnInfo findColumn(List<XColumnInfo> cmlist,
            String name) {
        for (int i=0; i<cmlist.size(); ++i) {
            XColumnInfo cur = cmlist.get(i);
            if (name.equalsIgnoreCase(cur.getName()))
                return cur;
        }
        return null;
    }

}
