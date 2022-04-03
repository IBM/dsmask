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
 * Inline script embedded in the masking pipeline.
 * Takes an input vector of values and produces an output vector of values.
 * Inline scripts are typically used for small snippets like predicate computation.
 * Re-usable scripts should be defined as @MaskingFunction objects,
 * and called through @StepFunction.
 * @author zinal
 */
public class StepScript extends StepBase {

    private final String body;

    public StepScript(String name, StepGroup owner, String body) {
        super(name, owner);
        this.body = body;
    }

    public String getBody() {
        return body;
    }

    @Override
    public StepType getType() {
        return StepType.Script;
    }

    @Override
    public boolean isIterable() {
        // iterations are not supported by inline scripts
        return false;
    }

    @Override
    public int hashCode() {
        int hash = super.hashCode();
        hash = 13 * hash + Objects.hashCode(this.body);
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
        if (super.equals(obj)) {
            return false;
        }
        final StepScript other = (StepScript) obj;
        return Objects.equals(this.body, other.body);
    }

}
