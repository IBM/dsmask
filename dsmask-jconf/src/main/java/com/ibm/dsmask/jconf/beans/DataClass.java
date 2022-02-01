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

import java.util.Objects;

/**
 * Data class description
 * @author zinal
 */
public class DataClass implements Comparable<DataClass> {

    private String name;
    private DataClassMode mode;

    public DataClass() {
        this.name = Utils.NONE;
        this.mode = DataClassMode.Normal;
    }

    public DataClass(String name, DataClassMode mode) {
        this.name = Utils.lower(name);
        this.mode = mode;
    }

    public DataClass(String name, String mode) {
        this.name = Utils.lower(name);
        this.mode = DataClassMode.getMode(mode);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = Utils.lower(name);
    }

    public DataClassMode getMode() {
        return mode;
    }

    public void setMode(DataClassMode mode) {
        this.mode = mode;
    }

    public boolean isConfidential() {
        return DataClassMode.Confidential.equals(mode);
    }

    public boolean isGroup() {
        return DataClassMode.Group.equals(mode);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + Objects.hashCode(this.name);
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
        final DataClass other = (DataClass) obj;
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        if (this.mode != other.mode) {
            return false;
        }
        return true;
    }

    @Override
    public int compareTo(DataClass o) {
        return this.name.compareTo(o.name);
    }

}
