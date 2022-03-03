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
package net.dsmask.beans;

import java.util.Objects;

/**
 * A single step in a masking algorithm pipeline,
 * calling a masking function for its input,
 * and delivering the results to its output.
 * @author zinal
 */
public class ItemStep extends ItemBase {

    private final MaskingFunction function;

    public ItemStep(String name, MaskingFunction function) {
        super(name);
        this.function = function;
    }

    public MaskingFunction getFunction() {
        return function;
    }

    @Override
    public ItemType getType() {
        return ItemType.Step;
    }

    @Override
    public boolean isIterable() {
        return function.isIterable();
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ItemStep other = (ItemStep) obj;
        if (!Objects.equals(this.function, other.function)) {
            return false;
        }
        return true;
    }

}
