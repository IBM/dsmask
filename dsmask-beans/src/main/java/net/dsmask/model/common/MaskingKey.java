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
package net.dsmask.model.common;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * A masking key - named string which is used as a key.
 * Used by some masking algorithms.
 * @author zinal
 */
public class MaskingKey extends EntityBase {

    private final String value;
    private final byte[] valueBits;

    public MaskingKey(String name, String value) {
        super(EntityType.Key, name);
        this.value = (value==null) ? "iddqd" : value;
        this.valueBits = this.value.getBytes(StandardCharsets.UTF_8);
    }

    public String getValue() {
        return value;
    }

    public byte[] getValueBits() {
        return valueBits;
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
        final MaskingKey other = (MaskingKey) obj;
        if (!Objects.equals(this.value, other.value)) {
            return false;
        }
        return true;
    }

}
