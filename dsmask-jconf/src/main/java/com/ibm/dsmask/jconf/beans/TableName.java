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
package com.ibm.dsmask.jconf.beans;

import java.util.Objects;

/**
 * Name of a single table.
 * @author zinal
 */
public class TableName {

    protected String database;
    protected String name;

    public TableName() {
        this.database = Utils.NONE;
        this.name = Utils.NONE;
    }

    public TableName(String database, String name) {
        this.database = Utils.lower(database);
        this.name = Utils.lower(name);
    }

    /**
     * @return Database name
     */
    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = Utils.lower(database);
    }

    /**
     * @return Table name (with schema, no database name)
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = Utils.lower(name);
    }

    /**
     * @return Full table name, including both schema and database name.
     */
    public String getFullName() {
        if (database==null || database.length()==0)
            return "default." + name;
        return database + "." + name;
    }

    public boolean isValid() {
        return name!=null && name.length()>0;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 67 * hash + Objects.hashCode(this.database);
        hash = 67 * hash + Objects.hashCode(this.name);
        return hash;
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
        final TableName other = (TableName) obj;
        if (!Objects.equals(this.database, other.database)) {
            return false;
        }
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "TableName{" + database + "." + name + '}';
    }

}
