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
 * Reference to an already computed value.
 * Starting position number is 1.
 * Normally the position is taken from the previous vector (empty name).
 * It can also be taken from any of the preceeding steps (non-empty name).
 * There is a pre-defined name "$", which always refers to the initial input vector.
 */
public class ValueRef {

    private final ItemBase item;
    private final int position;

    public ValueRef(ItemBase item, int position) {
        this.item = item;
        this.position = position;
    }

    public ItemBase getItem() {
        return item;
    }

    public int getPosition() {
        return position;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 59 * hash + Objects.hashCode(this.item);
        hash = 59 * hash + this.position;
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
        final ValueRef other = (ValueRef) obj;
        if (this.position != other.position) {
            return false;
        }
        if (!Objects.equals(this.item, other.item)) {
            return false;
        }
        return true;
    }

} // class Ref
