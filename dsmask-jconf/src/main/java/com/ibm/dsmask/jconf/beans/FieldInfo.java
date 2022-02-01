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

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import com.ibm.dsmask.jconf.impl.*;

/**
 * The description of a single field.
 * A field has a name and a set of associated data classes.
 * Data type topics are handled separately, no need for type
 * information here.
 * @author zinal
 */
public class FieldInfo {

    private String name;
    private final Set<String> dcs = new HashSet<>();

    public FieldInfo() {
        this.name = Utils.NONE;
    }

    public FieldInfo(String name) {
        this.name = Utils.lower(name);
    }

    public FieldInfo(String name, Collection<String> dcs) {
        this.name = Utils.lower(name);
        for (String dcname : dcs)
            this.dcs.add(Utils.lower(dcname));
    }

    public FieldInfo(String name, String[] dcs) {
        this.name = Utils.lower(name);
        for (String dcname : dcs)
            this.dcs.add(Utils.lower(dcname));
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = Utils.lower(name);
    }

    public Set<String> getDcs() {
        return Collections.unmodifiableSet(dcs);
    }

    public String getDcsString() {
        if (dcs.isEmpty())
            return "";
        final StringBuilder sb = new StringBuilder();
        boolean delim = false;
        for (String dc : dcs) {
            if (dc==null || dc.length()==0)
                continue;
            if (delim) sb.append(","); else delim = true;
            sb.append(dc);
        }
        return sb.toString();
    }

    public void setDcs(Collection<String> vals) {
        dcs.clear();
        if (vals!=null) {
            for (String dc : vals)
                dcs.add(Utils.lower(dc));
        }
    }

    public boolean addDc(String dc) {
        return dcs.add(Utils.lower(dc));
    }

    public boolean isValid() {
        return name!=null && name.length()>0;
    }

    public boolean isConfidential(DataClassRegistry dcr) {
        for (String dcname : dcs) {
            final DataClass dcref = dcr.find(dcname);
            if (dcref!=null && dcref.isConfidential())
                return true;
        }
        return false;
    }

    public Set<String> getConfClasses(DataClassRegistry dcr) {
        final Set<String> retval = new TreeSet<>();
        for (String dcname : dcs) {
            final DataClass dcref = dcr.find(dcname);
            if (dcref!=null && dcref.isConfidential())
                retval.add(dcname);
        }
        return retval;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.name);
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
        final FieldInfo other = (FieldInfo) obj;
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        if (!Objects.equals(this.dcs, other.dcs)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return name;
    }

}
