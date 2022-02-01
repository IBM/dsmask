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
package net.dsmask.model.common;

import net.dsmask.model.any.Utils;

/**
 * Parameter definition for a data masking algorithm.
 * @author zinal
 */
public class MaskingParameter implements ModelIdentity {

    private final String name;
    private final ParameterType type;
    private final String defval;

    public MaskingParameter(String name) {
        this(name, ParameterType.Line, null);
    }

    public MaskingParameter(String name, ParameterType type) {
        this(name, type, null);
    }

    public MaskingParameter(String name, ParameterType type, String defval) {
        this.name = Utils.safe(name);
        this.type = (type == null) ? ParameterType.Line : type;
        this.defval = defval;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getId() {
        return Utils.lower(name);
    }

    public ParameterType getType() {
        return type;
    }

    public String getDefval() {
        return defval;
    }

    public boolean isMandatory() {
        return (defval==null);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 23 * hash + Utils.hashCode(this.name);
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
        final MaskingParameter other = (MaskingParameter) obj;
        if (!Utils.equalsCI(this.name, other.name)) {
            return false;
        }
        if (!Utils.equalsNL(this.defval, other.defval)) {
            return false;
        }
        if (this.type != other.type) {
            return false;
        }
        return true;
    }

}
