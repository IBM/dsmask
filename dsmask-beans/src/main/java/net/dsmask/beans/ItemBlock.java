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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import net.dsmask.model.ModelUtils;

/**
 * Blocked sequence of masking operations.
 * Transforms input vector of values into the output vector of values by calling
 * a sequence of other transformations in the order specified.
 * @author zinal
 */
public class ItemBlock extends ItemBase {

    private final ItemBlock owner;
    private final List<ItemBase> items = new ArrayList<>();

    public ItemBlock() {
        super("");
        this.owner = null;
    }

    public ItemBlock(String name, ItemBlock owner) {
        super(name);
        this.owner = owner;
    }

    @Override
    public ItemType getType() {
        return ItemType.Block;
    }

    @Override
    public boolean isIterable() {
        // block supports iterations if any of its items supports it
        for (ItemBase ib : items)
            if (ib.isIterable())
                return true;
        return false;
    }

    public ItemBlock getOwner() {
        return owner;
    }

    public List<ItemBase> getItems() {
        return Collections.unmodifiableList(items);
    }

    public ItemBlock addItem(ItemBase ib) {
        this.items.add(ib);
        return this;
    }

    /**
     * Find the item with the specified name.
     * Null or empty string on input means the last item.
     * "$" name means the initial value of the topmost owner block.
     * "#" name means the initial value of the current block.
     * @param name Item name to look for.
     * @return Item reference, or null if none found.
     */
    public ItemBase findItem(String name) {
        name = ModelUtils.lower(name);
        if (name.length() == 0) {
            if (items.isEmpty())
                return null;
            return items.get(items.size() - 1);
        }
        if ("#".equalsIgnoreCase(name)) {
            return this;
        }
        if ("$".equalsIgnoreCase(name)) {
            ItemBlock block = this;
            while (block.getOwner() != null)
                block = block.getOwner();
            return block;
        }
        ItemBase item = null;
        ItemBlock handler = this;
        while (item == null && handler != null) {
            item = handler.lookupItem(name);
            handler = handler.getOwner();
        }
        return item;
    }

    /**
     * Find the last item with the name specified.
     * @param name Normalized name (no nulls, no empty strings, lower case)
     * @return Item with the name specified, or null
     */
    protected final ItemBase lookupItem(String name) {
        ItemBase item = null;
        for (ItemBase ib : items) {
            if (name.equalsIgnoreCase(ib.getName()))
                item = ib;
        }
        return item;
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
        final ItemBlock other = (ItemBlock) obj;
        if (!Objects.equals(this.items, other.items)) {
            return false;
        }
        return true;
    }

}
