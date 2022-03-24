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
 * Inline script embedded in the masking pipeline.
 * Takes an input vector of values and produces an output vector of values.
 * Inline scripts are typically used for small snippets like predicate computation.
 * Re-usable scripts should be defined as MaskingFunction objects,
 * and called through ItemStep.
 * @author zinal
 */
public class ItemScript extends ItemBase {

    private final String body;

    public ItemScript(String name, String body) {
        super(name);
        this.body = body;
    }

    public String getBody() {
        return body;
    }

    @Override
    public ItemType getType() {
        return ItemType.Script;
    }

    @Override
    public boolean isIterable() {
        // iterations are not supported by inline scripts
        return false;
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
        final ItemScript other = (ItemScript) obj;
        if (!Objects.equals(this.body, other.body)) {
            return false;
        }
        return true;
    }

}
