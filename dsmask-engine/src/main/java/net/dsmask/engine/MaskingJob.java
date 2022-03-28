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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.dsmask.model.*;

/**
 *
 * @author zinal
 */
public class MaskingJob implements Runnable {

    private final LinkInput input;
    private final LinkOutput output;
    private final MaskingEngine engine;

    // maps output column index -> input column metadata
    //  (including index and name)
    private final Map<Integer, XColumnInfo> indexMap = new HashMap<>();
    // missing columns
    private List<String> missingColumns = null;

    public MaskingJob(LinkInfo linkInfo, MaskingProfile profile, int batchSize) {
        this.input = linkInfo.getInputLink();
        this.output = linkInfo.getOutputLink();
        this.engine = new MaskingEngine(linkInfo, profile, batchSize);
        buildIndexMap();
    }

    public List<String> getMissingColumns() {
        if (missingColumns==null)
            return Collections.emptyList();
        return missingColumns;
    }

    @Override
    public void run() {
        RowInput row;
        while ((row = input.readRecord()) != null) {
            if (! engine.addRow(row)) {
                computeAndSend();
            }
        }
        computeAndSend();
    }

    private void computeAndSend() {
        engine.compute();
        final int nrows = engine.getRowCount();
        for (int i=0; i<nrows; ++i) {
            engine.collect(i, output);
        }
    }

    /**
     * Generate an index map for later use.
     * In case some columns are missing, poplulate the list of missing
     * column names for diagnostic.
     * @return true, if all necessary columns were mapped, false otherwise.
     */
    private boolean buildIndexMap() {
        // index map is necessary to properly construct output records
        indexMap.clear();
        final List<XColumnInfo> inputColumns = engine.getLinkInfo().getInputColumns();
        final List<XColumnInfo> outputColumns = engine.getLinkInfo().getOutputColumns();
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

}
