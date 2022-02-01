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
package com.ibm.dsmask.apply;

import java.util.ArrayList;
import java.util.List;
import com.ibm.is.cc.javastage.api.*;

/**
 *
 * @author zinal
 */
public class DstMergeGroup {

    private final String targetColumnName;
    private final int targetColumnIndex;
    private final List<Column> sourceColumns = new ArrayList<>();

    public DstMergeGroup(String name, int index) {
        this.targetColumnName = name;
        this.targetColumnIndex = index;
    }

    public String getTargetColumnName() {
        return targetColumnName;
    }

    public int getTargetColumnIndex() {
        return targetColumnIndex;
    }

    public List<Column> getSourceColumns() {
        return sourceColumns;
    }

    public void addColumn(ColumnMetadata meta, String prefix) {
        sourceColumns.add(new Column(meta, prefix));
    }

    public static class Column {
        private final ColumnMetadata meta;
        private final String name;
        private final int index;

        public Column(ColumnMetadata meta, String prefix) {
            this.meta = meta;
            this.name = (prefix==null) ? meta.getName()
                    : meta.getName().substring(prefix.length());
            this.index = meta.getIndex();
        }

        public ColumnMetadata getMeta() {
            return meta;
        }

        public String getName() {
            return name;
        }

        public int getIndex() {
            return index;
        }
    }

}
