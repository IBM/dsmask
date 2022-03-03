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
package net.dsmask.beans;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;
import net.dsmask.model.*;

/**
 * Description of a single table.
 * Here a table is treated as a named collection of fields.
 * @author zinal
 */
public class MetaTable implements AnyTable {

    private final String databaseName;
    private final String schemaName;
    private final String tableName;
    private final LinkedHashMap<String, MetaField> fields = new LinkedHashMap<>();
    private final boolean ignoreCase;

    public MetaTable(String fullTableName) {
        this(fullTableName, false);
    }

    public MetaTable(String fullTableName, boolean ignoreCase) {
        if (StringUtils.isBlank(fullTableName)) {
            this.databaseName = null;
            this.schemaName = null;
            this.tableName = null;
        } else {
            String[] parts = StringUtils.split(fullTableName, ".", 3);
            switch (parts.length) {
                case 3:
                    this.databaseName = parts[0];
                    this.schemaName = parts[1];
                    this.tableName = parts[2];
                    break;
                case 2:
                    this.databaseName = null;
                    this.schemaName = parts[0];
                    this.tableName = parts[1];
                    break;
                case 1:
                    this.databaseName = null;
                    this.schemaName = null;
                    this.tableName = parts[0];
                    break;
                default:
                    this.databaseName = null;
                    this.schemaName = null;
                    this.tableName = null;
                    break;
            }
        }
        this.ignoreCase = ignoreCase;
    }

    public MetaTable(String databaseName, String tableNameWithSchema) {
        this(databaseName, tableNameWithSchema, false);
    }

    public MetaTable(String databaseName, String tableNameWithSchema, boolean ignoreCase) {
        this.databaseName = databaseName;
        if (StringUtils.isBlank(tableNameWithSchema)) {
            this.schemaName = null;
            this.tableName = null;
        } else {
            String[] parts = StringUtils.split(tableNameWithSchema, ".", 2);
            switch (parts.length) {
                case 2:
                    this.schemaName = parts[0];
                    this.tableName = parts[1];
                    break;
                case 1:
                    this.schemaName = null;
                    this.tableName = parts[0];
                    break;
                default:
                    this.schemaName = null;
                    this.tableName = null;
                    break;
            }
        }
        this.ignoreCase = ignoreCase;
    }

    public MetaTable(String databaseName, String schemaName, String tableName) {
        this(databaseName, schemaName, tableName, false);
    }

    public MetaTable(String databaseName, String schemaName, String tableName,
            boolean ignoreCase) {
        this.databaseName = ModelUtils.safe(databaseName);
        this.schemaName = ModelUtils.safe(schemaName);
        this.tableName = ModelUtils.safe(tableName);
        this.ignoreCase = ignoreCase;
    }

    @Override
    public String getDatabaseName() {
        return databaseName;
    }

    @Override
    public String getSchemaName() {
        return schemaName;
    }

    @Override
    public String getTableName() {
        return tableName;
    }

    @Override
    public boolean isIgnoreCase() {
        return ignoreCase;
    }

    @Override
    public String getFullName() {
        final StringBuilder sb = new StringBuilder();
        if (databaseName != null && databaseName.length() > 0) {
            sb.append(databaseName);
        }
        if (schemaName != null && schemaName.length() > 0) {
            if (sb.length() > 0)
                sb.append(".");
            sb.append(schemaName);
        }
        if (sb.length() > 0)
            sb.append(".");
        if (tableName != null)
            sb.append(tableName);
        return sb.toString();
    }

    @Override
    public Collection<MetaField> getFields() {
        return Collections.unmodifiableCollection(fields.values());
    }

    public MetaTable addField(MetaField mf) {
        fields.put(ignoreCase ? mf.getName().toLowerCase() : mf.getName(), mf);
        return this;
    }

    public MetaTable addFields(Collection<MetaField> mfs) {
        if (mfs!=null) {
            for (MetaField mf : mfs) {
                fields.put(ignoreCase ? mf.getName().toLowerCase() : mf.getName(), mf);
            }
        }
        return this;
    }

    public MetaField findField(String fn) {
        for (MetaField fi : fields.values()) {
            if (fi.getName().equals(fn))
                return fi;
        }
        return null;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 23 * hash + Objects.hashCode(this.databaseName);
        hash = 23 * hash + Objects.hashCode(this.schemaName);
        hash = 23 * hash + Objects.hashCode(this.tableName);
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
        final MetaTable other = (MetaTable) obj;
        if (this.ignoreCase != other.ignoreCase) {
            return false;
        }
        if (this.ignoreCase) {
            if (!ModelUtils.equalsCI(this.databaseName, other.databaseName)) {
                return false;
            }
            if (!ModelUtils.equalsCI(this.schemaName, other.schemaName)) {
                return false;
            }
            if (!ModelUtils.equalsCI(this.tableName, other.tableName)) {
                return false;
            }
        } else {
            if (!ModelUtils.safeEquals(this.databaseName, other.databaseName)) {
                return false;
            }
            if (!ModelUtils.safeEquals(this.schemaName, other.schemaName)) {
                return false;
            }
            if (!ModelUtils.safeEquals(this.tableName, other.tableName)) {
                return false;
            }
        }
        // TODO: compare field names depending on the ignoreCi flag
        if (!Objects.equals(this.fields, other.fields)) {
            return false;
        }
        return true;
    }

}
