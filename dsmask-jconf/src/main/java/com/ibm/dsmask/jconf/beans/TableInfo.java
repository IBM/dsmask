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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Description of a single table.
 * Here a table is treated as a named collection of fields.
 * @author zinal
 */
public class TableInfo extends TableName {

    private final Map<String, FieldInfo> fields = new HashMap<>();

    public TableInfo() {
        super();
    }

    public TableInfo(String database, String name) {
        super(database, name);
    }

    public TableInfo(String database, String name,
            Collection<FieldInfo> fields) {
        super(database, name);
        for (FieldInfo fi : fields)
            this.fields.put(fi.getName(), fi);
    }

    public List<FieldInfo> getFields() {
        return new ArrayList<>(fields.values());
    }

    public void setFields(List<FieldInfo> fds) {
        fields.clear();
        if (fds!=null) {
            for (FieldInfo fi : fds)
                fields.put(fi.getName(), fi);
        }
    }

    public FieldInfo addField(FieldInfo fi) {
        fields.put(fi.getName(), fi);
        return fi;
    }

    public FieldInfo getField(String fieldName) {
        return fields.get(Utils.lower(fieldName));
    }

    public List<FieldInfo> findFields(String... fieldNames) {
        final ArrayList<FieldInfo> retval = new ArrayList<>();
        for (String fn : fieldNames) {
            FieldInfo fi = fields.get(Utils.lower(fn));
            if (fi==null) {
                throw new IllegalArgumentException("Unknown field name ["
                        + fn + "] in table [" + name + "]");
            }
            retval.add(fi);
        }
        return retval;
    }

    public Set<String> getAllDataClasses() {
        final Set<String> dataClasses = new HashSet<>();
        for (FieldInfo fi : fields.values())
            dataClasses.addAll(fi.getDcs());
        dataClasses.remove("");
        return dataClasses;
    }

    public List<FieldInfo> getCondidentialFields(DataClassLookup dcr) {
        final List<FieldInfo> retval = new ArrayList<>();
        for (FieldInfo fi : fields.values()) {
            for (String dcname : fi.getDcs()) {
                final DataClass dcref = dcr.find(dcname);
                if (dcref!=null && dcref.isConfidential()) {
                    retval.add(fi);
                    break; // no need to check other data classes
                }
            }
        }
        return retval;
    }

    @Override
    public boolean isValid() {
        return super.isValid() && !fields.isEmpty();
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 67 * hash + Objects.hashCode(this.database);
        hash = 67 * hash + Objects.hashCode(this.name);
        hash = 67 * hash + Objects.hashCode(this.fields);
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
        final TableInfo other = (TableInfo) obj;
        if (!Objects.equals(this.database, other.database)) {
            return false;
        }
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        if (!Objects.equals(this.fields, other.fields)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "TableInfo{" + database + "." + name + ", " + fields.keySet() + '}';
    }

}
