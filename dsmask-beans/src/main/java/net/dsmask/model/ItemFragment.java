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

import java.util.Objects;

/**
 * Reference to a fragment to be used in another execution pipeline.
 * Passes the input to the specified fragment and returns the fragment's output.
 * @author zinal
 */
public class ItemFragment extends ItemBase {

    private final MaskingFragment fragment;

    public ItemFragment(String name, MaskingFragment fragment) {
        super(name);
        this.fragment = fragment;
    }

    public MaskingFragment getFragment() {
        return fragment;
    }

    @Override
    public ItemType getType() {
        return ItemType.Fragment;
    }

    @Override
    public boolean isIterable() {
        return fragment.getPipeline().isIterable();
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
        final ItemFragment other = (ItemFragment) obj;
        if (!Objects.equals(this.fragment, other.fragment)) {
            return false;
        }
        return true;
    }

}
