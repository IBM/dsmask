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
package com.ibm.dsmask.mock;

import java.util.ArrayList;
import java.util.List;
import com.ibm.is.cc.javastage.api.ColumnMetadata;

/**
 *
 * @author zinal
 */
public class MockData extends MaskingMockData {

    private List<ColumnMetadata> columnMetadata = null;

    public MockData(String tableName) {
        super(tableName);
    }

    public List<ColumnMetadata> getColumnMetadata() {
        if (columnMetadata==null) {
            columnMetadata = new ArrayList<>();
            for (MaskingMockColumn kmc : getColumns()) {
                columnMetadata.add(new ColumnMetaMock(kmc.getNumber(), kmc.getName()));
            }
        }
        return columnMetadata;
    }
}
