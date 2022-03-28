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
public class Workspace {

    private final LinkInput inputLink;
    private final LinkOutput outputLink;
    private final MaskingProfile profile;
    
    private int batchSize = 100;
    
    private final Map<String, Integer> inputMap;
    private final Map<String, Integer> outputMap;

    // maps output column index -> input column metadata
    //  (including index and name)
    private final Map<Integer, XColumnInfo> indexMap = new HashMap<>();
    // missing columns
    private List<String> missingColumns = null;

    public Workspace(LinkInput input, LinkOutput output, MaskingProfile profile) {
        this.inputLink = input;
        this.outputLink = output;
        this.profile = profile;
        this.inputMap = map(input.getInputColumns());
        this.outputMap = map(input.getInputColumns());
        buildIndexMap();
    }

    public final MaskingProfile getProfile() {
        return profile;
    }
    
    public final LinkInput getInputLink() {
        return inputLink;
    }

    public final LinkOutput getOutputLink() {
        return outputLink;
    }

    public final int getBatchSize() {
        return batchSize;
    }

    public final void setBatchSize(int batchSize) {
        this.batchSize = ((batchSize > 0) && (batchSize<=1000000)) ? batchSize : 1;
    }

    public final Map<String, Integer> getInputMap() {
        return inputMap;
    }

    public final Map<String, Integer> getOutputMap() {
        return outputMap;
    }

    public final List<XColumnInfo> getInputColumns() {
        return inputLink.getInputColumns();
    }

    public final List<XColumnInfo> getOutputColumns() {
        return outputLink.getOutputColumns();
    }
    
    public final List<MaskingOperation> getOperations() {
        return profile.getOperations();
    }

    public List<String> getMissingColumns() {
        if (missingColumns==null)
            return Collections.emptyList();
        return missingColumns;
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
        final List<XColumnInfo> inputColumns = inputLink.getInputColumns();
        final List<XColumnInfo> outputColumns = outputLink.getOutputColumns();
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
     * Build a name-to-index map for the columns list.
     * @param columns Columns list
     * @return name-to-index map
     */
    private static Map<String, Integer> map(List<XColumnInfo> columns) {
        final Map<String, Integer> m = new HashMap<>();
        for (XColumnInfo ci : columns)
            m.put(ci.getName(), ci.getIndex());
        return m;
    }

}
