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
 * Fragment is a named masking procedure, which encapsulates
 * the part of the execution pipeline to be reused between
 * different masking rules.
 * @author zinal
 */
public class MaskingFragment extends EntityBase {

    private final StepRoot pipeline = new StepRoot();

    public MaskingFragment(String name) {
        super(name);
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.Fragment;
    }

    public StepRoot getPipeline() {
        return pipeline;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (! super.equals(obj)) {
            return false;
        }
        final MaskingFragment other = (MaskingFragment) obj;
        if (!Objects.equals(this.pipeline, other.pipeline)) {
            return false;
        }
        return true;
    }

}
