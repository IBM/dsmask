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

import java.util.Objects;

/**
 * Reference to a fragment to be used in another execution pipeline.
 * Passes the input to the specified fragment and returns the fragment's output.
 * @author zinal
 */
public class StepFragment extends StepBase {

    private final MaskingFragment fragment;

    public StepFragment(String name, StepGroup owner, MaskingFragment fragment) {
        super(name, owner);
        this.fragment = fragment;
    }

    public MaskingFragment getFragment() {
        return fragment;
    }

    @Override
    public StepType getType() {
        return StepType.Fragment;
    }

    @Override
    public boolean isIterable() {
        return fragment.getPipeline().isIterable();
    }

    @Override
    public int hashCode() {
        int hash = super.hashCode();
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
        final StepFragment other = (StepFragment) obj;
        return this.fragment == other.fragment;
    }

}
