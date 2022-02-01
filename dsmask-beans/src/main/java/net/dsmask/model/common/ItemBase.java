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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.dsmask.model.any.Utils;

/**
 * A transformation over an input vector of values, producing an output vector of values.
 * Can be a single call to the MaskingFunction (ItemStep),
 * an inline script (ItemScript),
 * a sequence of calls to other transformations (ItemBlock),
 * or a reference to the re-usable fragment of transformations (ItemFragment).
 * @author zinal
 */
public abstract class ItemBase implements ModelIdentity {

    private final String name;
    private final List<ValueRef> predicates = new ArrayList<>();
    private final List<ValueRef> inputs = new ArrayList<>();
    private UniqCheck uniqCheck = null;

    public ItemBase(String name) {
        this.name = Utils.safe(name);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getId() {
        return Utils.lower(name);
    }

    /**
     * @return Id of an actual type of item, for simpler handling.
     */
    public abstract ItemType getType();

    /**
     * @return true, if masking iterations are supported, false otherwise.
     */
    public abstract boolean isIterable();

    public List<ValueRef> getInputs() {
        return inputs;
    }

    public void setInputs(List<ValueRef> refs) {
        this.inputs.clear();
        if (refs!=null)
            this.inputs.addAll(refs);
    }

    public ItemBase addInput(ValueRef ref) {
        inputs.add(ref);
        return this;
    }

    public List<ValueRef> getPredicates() {
        return predicates;
    }

    public void setPredicates(List<ValueRef> preds) {
        this.predicates.clear();
        if (preds!=null)
            this.predicates.addAll(preds);
    }

    public ItemBase addPredicate(ValueRef pred) {
        predicates.add(pred);
        return this;
    }

    public UniqCheck getUniqCheck() {
        return uniqCheck;
    }

    public void setUniqCheck(UniqCheck uniqCheck) {
        this.uniqCheck = uniqCheck;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 79 * hash + Utils.hashCode(this.name);
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
        final ItemBase other = (ItemBase) obj;
        if (!Utils.equalsCI(this.name, other.name)) {
            return false;
        }
        if (!Objects.equals(this.predicates, other.predicates)) {
            return false;
        }
        if (!Objects.equals(this.inputs, other.inputs)) {
            return false;
        }
        if (!Objects.equals(this.uniqCheck, other.uniqCheck)) {
            return false;
        }
        return true;
    }


}
