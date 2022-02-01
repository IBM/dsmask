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
package com.ibm.dsmask.jconf.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import com.ibm.dsmask.jconf.beans.*;
import java.util.Arrays;

/**
 *
 * @author zinal
 */
public class DataClassRegistry implements DataClassLookup {

    protected final Map<String, DataClass> entries = new HashMap<>();

    public DataClassRegistry() {
    }

    public DataClassRegistry(Collection<DataClass> dcs) {
        for (DataClass dc : dcs)
            entries.put(dc.getName(), dc);
    }

    protected final Map<String, DataClass> getEntries() {
        return entries;
    }

    protected final void add(DataClass dcref) {
        if (dcref!=null)
            entries.put(dcref.getName(), dcref);
    }

    protected final void addAll(Collection<DataClass> dcs) {
        if (dcs!=null) {
            for (DataClass dcref : dcs)
                add(dcref);
        }
    }

    @Override
    public final int size() {
        return entries.size();
    }

    public void polulate() {
        // nothing here, this is to be overriden
    }

    @Override
    public final DataClass find(String name) {
        return entries.get(Utils.lower(name));
    }

    @Override
    public final DataClass[] collect() {
        DataClass[] v = entries.values().toArray(new DataClass[entries.size()]);
        Arrays.sort(v);
        return v;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + Objects.hashCode(this.entries);
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
        final DataClassRegistry other = (DataClassRegistry) obj;
        if (!Objects.equals(this.entries, other.entries)) {
            return false;
        }
        return true;
    }

}
