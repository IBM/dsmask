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
 * Sequence of masking operations.
 * Transforms the input vector of values into the output vector of values by calling
 * a sequence of other transformations in the order specified.
 * @author zinal
 */
public class StepBlock extends StepBase implements StepGroup {

    private final List<StepBase> items = new ArrayList<>();

    public StepBlock(String name, StepGroup owner) {
        super(name, owner);
    }

    @Override
    public StepType getType() {
        return StepType.Block;
    }

    @Override
    public boolean isIterable() {
        // block supports iterations if any of its items supports it
        for (StepBase sb : items)
            if (sb.isIterable())
                return true;
        return false;
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
        int hash = super.hashCode();
        hash = 31 * hash + Objects.hashCode(this.items);
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
        if (! super.equals(obj)) {
            return false;
        }
        final StepBlock other = (StepBlock) obj;
        return Objects.equals(this.items, other.items);
    }

}
