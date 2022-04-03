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
 *
 * @author zinal
 */
public class StepRoot implements StepGroup {

    private final List<StepBase> items = new ArrayList<>();

    @Override
    public StepType getType() {
        return StepType.Root;
    }

    @Override
    public boolean isIterable() {
        // root supports iterations if any of its items supports it
        for (StepBase sb : items)
            if (sb.isIterable())
                return true;
        return false;
    }

    @Override
    public StepGroup getOwner() {
        // Root step does not have the owner
        return null;
    }

    @Override
    public StepAny findItem(String name) {
        return StepBase.lookup(this, null, name);
    }

    @Override
    public List<StepBase> getItems() {
        return items;
    }

    @Override
    public StepGroup addItem(StepBase sb) {
        this.items.add(sb);
        return this;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + Objects.hashCode(this.items);
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
        final StepRoot other = (StepRoot) obj;
        return Objects.equals(this.items, other.items);
    }

    @Override
    public String toString() {
        return "StepRoot";
    }

}
