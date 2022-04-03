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
package net.dsmask.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A transformation over an input vector of values, producing an output vector of values.
 * Can be a single call to the MaskingFunction (@StepFunction),
 * an inline script (@StepScript),
 * a sequence of calls to other transformations (@StepBlock),
 * or a reference to the re-usable fragment of transformations (@StepFragment).
 * @author zinal
 */
public abstract class StepBase implements StepAny, ModelIdentity {

    private final StepGroup owner;
    private final String name;
    private final List<ValueRef> predicates = new ArrayList<>();
    private final List<ValueRef> inputs = new ArrayList<>();
    private UniqCheck uniqCheck = null;

    public StepBase(String name, StepGroup owner) {
        this.name = ModelUtils.safe(name);
        this.owner = owner;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getId() {
        return ModelUtils.lower(name);
    }

    @Override
    public StepGroup getOwner() {
        return owner;
    }

    public List<ValueRef> getInputs() {
        return inputs;
    }

    public void setInputs(List<ValueRef> refs) {
        this.inputs.clear();
        if (refs!=null)
            this.inputs.addAll(refs);
    }

    public StepBase addInput(ValueRef ref) {
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

    public StepBase addPredicate(ValueRef pred) {
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
    public StepAny findItem(String name) {
        return StepBase.lookup(owner, this, name);
    }

    /**
     * Find the step with the specified name withing the specified group.
     * Null or empty string on input means the last item computed before the context.
     * "$" name means the input value of the topmost owner block.
     * "#" name means the input value of the current block.
     * @param group Step group
     * @param self The context of the lookup
     * @param name Step name to find
     * @return The referenced step, or null if it was not found
     */
    public static StepAny lookup(StepGroup group, StepAny self, String name) {
        // Input validation and normalization
        if (group==null) {
            if (self==null) {
                throw new IllegalArgumentException("lookup(): group and self cannot be both null");
            }
            group = self.getOwner();
            if (group == null) {
                // Lookup in the root group passed as self.
                group = (StepRoot) self;
                self = null;
            }
        } else {
            if (self != null && group != self.getOwner()) {
                throw new IllegalArgumentException("lookup(): self must be part of the passed group");
            }
        }
        name = ModelUtils.lower(name);
        if (name.length() == 0) {
            // Reference to the result of the group - e.g. the output of the last step
            if (self==null) {
                final List<? extends StepBase> items = group.getItems();
                return items.isEmpty() ? null : items.get(items.size()-1);
            }
            // Find the step preceeding self.
            StepBase prev = null;
            for (StepBase cur : group.getItems()) {
                if (cur == self) {
                    // The input of the first step is the owner's value
                    return (prev==null) ? self.getOwner() : prev;
                }
                prev = cur;
            }
            // This should not happen.
            throw new IllegalStateException("lookup(): search passed out of the group");
        }
        if ("#".equalsIgnoreCase(name)) {
            return group;
        }
        if ("$".equalsIgnoreCase(name)) {
            StepGroup block = group.getOwner();
            while (block.getOwner() != null)
                block = block.getOwner();
            return block;
        }
        StepAny item = null;
        StepAny cur = self;
        StepGroup block = group;
        while (item == null && block != null) {
            item = lookupItem(block, cur, name);
            cur = block;
            block = block.getOwner();
        }
        return item;
    }

    /**
     * Find the last item with the name specified - but not at or after the element self.
     * @param group Group to search within
     * @param self Element to stop on
     * @param name Normalized name (no nulls, no empty strings, lower case)
     * @return Item with the name specified, or null
     */
    protected static StepAny lookupItem(StepGroup group, StepAny self, String name) {
        StepBase retval = null;
        for (StepBase sb : group.getItems()) {
            if (sb == self)
                break;
            if (name.equalsIgnoreCase(sb.getName()))
                retval = sb;
        }
        return retval;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 13 * hash + Objects.hashCode(this.name);
        hash = 13 * hash + Objects.hashCode(this.predicates);
        hash = 13 * hash + Objects.hashCode(this.inputs);
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
        final StepBase other = (StepBase) obj;
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        if (!Objects.equals(this.predicates, other.predicates)) {
            return false;
        }
        if (!Objects.equals(this.inputs, other.inputs)) {
            return false;
        }
        return this.uniqCheck == other.uniqCheck;
    }

    @Override
    public String toString() {
        return "Step." + getType() + "{" + name + " at " + owner + '}';
    }

}
