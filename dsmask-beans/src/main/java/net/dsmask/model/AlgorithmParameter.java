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
package net.dsmask.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import net.dsmask.util.DsMaskUtil;

/**
 * Parameter definition for a data masking algorithm.
 * @author zinal
 */
public class AlgorithmParameter implements ModelIdentity {

    private final String name;
    private final ParameterType type;
    private final String defval;
    private List<String> items;

    public AlgorithmParameter(String name) {
        this(name, ParameterType.Line, null);
    }

    public AlgorithmParameter(String name, ParameterType type) {
        this(name, type, null);
    }

    public AlgorithmParameter(String name, ParameterType type, String defval) {
        this.name = ModelUtils.safe(name);
        this.type = (type == null) ? ParameterType.Line : type;
        this.defval = defval;
        this.items = null;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getId() {
        return ModelUtils.lower(name);
    }

    public ParameterType getType() {
        return type;
    }

    public String getDefval() {
        return defval;
    }

    public boolean isMandatory() {
        // Default value may be an empty string.
        // Only null here means that there is no default value.
        return (defval==null);
    }

    public List<String> getItems() {
        return (items==null) ?
                Collections.emptyList() :
                Collections.unmodifiableList(items);
    }

    public void addItem(String item) {
        item = DsMaskUtil.lower(item);
        if (item.length() == 0) {
            throw new IllegalArgumentException(item);
        }
        if (items==null)
            items = new ArrayList<>();
        items.add(item);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 23 * hash + ModelUtils.hashCode(this.name);
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
        final AlgorithmParameter other = (AlgorithmParameter) obj;
        if (!ModelUtils.equalsCI(this.name, other.name)) {
            return false;
        }
        if (!ModelUtils.equalsNL(this.defval, other.defval)) {
            return false;
        }
        if (this.type != other.type) {
            return false;
        }
        if (!Objects.equals(this.items, other.items)) {
            return false;
        }
        return true;
    }

}
