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

import net.dsmask.model.*;

/**
 * Data class label definition.
 * @author zinal
 */
public class MaskingLabel extends EntityBase implements AnyLabel {

    private final LabelMode mode;

    public MaskingLabel(String name, LabelMode mode) {
        super(EntityType.Label, name);
        this.mode = (mode == null) ? LabelMode.Normal : mode;
    }

    public MaskingLabel(String name, String mode) {
        super(EntityType.Label, name);
        this.mode = LabelMode.getMode(mode);
    }

    @Override
    public LabelMode getMode() {
        return mode;
    }

    @Override
    public boolean isConfidential() {
        return LabelMode.Confidential.equals(mode);
    }

    @Override
    public boolean isGroup() {
        return LabelMode.Group.equals(mode);
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
        final MaskingLabel other = (MaskingLabel) obj;
        if (this.mode != other.mode) {
            return false;
        }
        return true;
    }

}
