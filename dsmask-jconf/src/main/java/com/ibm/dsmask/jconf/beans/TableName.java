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
import org.apache.commons.text.StringTokenizer;

/**
 * Name of a single table.
 * @author zinal
 */
public class TableName implements Comparable<TableName> {

    protected String database;
    protected String schema;
    protected String table;

    public TableName() {
        this.database = Utils.NONE;
        this.schema = Utils.NONE;
        this.table = Utils.NONE;
    }

    public TableName(String database, String schema, String table) {
        this.database = Utils.safe(database);
        this.schema = Utils.safe(schema);
        this.table = Utils.safe(table);
    }

    public TableName(String v) {
        final String[] items = 
                new StringTokenizer(Utils.safe(v), '.').getTokenArray();
        if (items.length == 0) {
            this.database = Utils.NONE;
            this.schema = Utils.NONE;
            this.table = Utils.NONE;
        } else if (items.length == 1) {
            this.database = Utils.NONE;
            this.schema = Utils.NONE;
            this.table = items[0];
        } else if (items.length == 2) {
            this.database = Utils.NONE;
            this.schema = items[0];
            this.table = items[1];
        } else if (items.length == 3) {
            this.database = items[0];
            this.schema = items[1];
            this.table = items[2];
        } else {
            throw new IllegalArgumentException("Incorrect full table name: " + v);
        }
    }

    public TableName(String database, String name) {
        this.database = Utils.safe(database);
        final String[] items = 
                new StringTokenizer(Utils.safe(name), '.').getTokenArray();
        if (items.length == 0) {
            this.schema = Utils.NONE;
            this.table = Utils.NONE;
        } else if (items.length == 1) {
            this.schema = Utils.NONE;
            this.table = items[0];
        } else if (items.length == 2) {
            this.schema = items[0];
            this.table = items[1];
        } else {
            throw new IllegalArgumentException("Incorrect table name: " + name);
        }
    }

    /**
     * @return Database name
     */
    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = Utils.safe(database);
    }

    /**
     * @return Schema name
     */
    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = Utils.safe(schema);
    }

    /**
     * @return Table name
     */
    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = Utils.safe(table);
    }

    /**
     * @return Table name (with schema, no database name)
     */
    public String getName() {
        return (schema!=null && schema.length() > 0) ?
                (schema + "." + table ) : table;
    }

    public void setName(String name) {
        final String[] items = 
                new StringTokenizer(Utils.safe(name), '.').getTokenArray();
        if (items.length == 0) {
            this.schema = Utils.NONE;
            this.table = Utils.NONE;
        } else if (items.length == 1) {
            this.schema = Utils.NONE;
            this.table = items[0];
        } else if (items.length == 2) {
            this.schema = items[0];
            this.table = items[1];
        } else {
            throw new IllegalArgumentException("Incorrect table name: " + name);
        }
    }

    /**
     * @return Full table name, including both schema and database name.
     */
    public String getFullName() {
        if (database==null || database.length()==0)
            return "default." + getName();
        return database + "." + getName();
    }

    public boolean isValid() {
        return table!=null && table.length()>0;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 67 * hash + Objects.hashCode(this.database);
        hash = 67 * hash + Objects.hashCode(this.schema);
        hash = 67 * hash + Objects.hashCode(this.table);
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
        if (!Objects.equals(this.schema, other.schema)) {
            return false;
        }
        if (!Objects.equals(this.table, other.table)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "TableName{" + getFullName() + '}';
    }

    @Override
    public int compareTo(TableName o) {
        if ( this == o )
            return 0;
        int cmp = this.database.compareTo(o.database);
        if (cmp != 0)
            return cmp;
        cmp = this.schema.compareTo(o.schema);
        if (cmp != 0)
            return cmp;
        cmp = this.table.compareTo(o.table);
        return cmp;
    }

}
