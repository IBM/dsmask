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
package com.ibm.dsmask.beans;

import com.ibm.dsmask.impl.Utils;
import java.util.ArrayList;
import java.util.List;

/**
 * Masked table bean.
 * @author zinal
 */
public class MskTable {

    private int id;
    private String databaseName;
    private String tableName;
    private final List<MskOp> operations = new ArrayList<>();

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public List<MskOp> getOperations() {
        return operations;
    }

    @Override
    public String toString() {
        return getTableInfo();
    }

    public final String getTableInfo() {
        return Utils.lower(databaseName) + "." + Utils.lower(tableName);
    }
}
