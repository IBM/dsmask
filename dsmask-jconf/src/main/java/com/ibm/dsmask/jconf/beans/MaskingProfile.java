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
package com.ibm.dsmask.jconf.beans;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;

/**
 * Table masking profile - a set of masking actions to be executed
 * over a specific table (chosen according to its fields data classes).
 * Combines references to table information, masking rules and
 * field access definitions.
 * @author zinal
 */
public class MaskingProfile {

    private TableInfo tableInfo;
    private List<MaskingOperation> operations;

    public TableInfo getTableInfo() {
        return tableInfo;
    }

    public void setTableInfo(TableInfo tableInfo) {
        this.tableInfo = tableInfo;
    }

    public List<MaskingOperation> getOperations() {
        return operations;
    }

    public void setOperations(List<MaskingOperation> operations) {
        this.operations = operations;
    }

    public boolean hasOperations() {
        return operations!=null && operations.isEmpty()==false;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        MaskingProfile other = (MaskingProfile) obj;
        if (!Objects.equals(this.tableInfo, other.tableInfo))
            return false;
        return Objects.equals(
                new HashSet<>(this.operations),
                new HashSet<>(other.operations));
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 61 * hash + Objects.hashCode(this.tableInfo);
        hash = 61 * hash + Objects.hashCode(this.operations);
        return hash;
    }

    @Override
    public String toString() {
        return "MaskingProfile{" + "ti=" + tableInfo + ", ops=" + operations + '}';
    }

}
