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
 * A single step in a masking algorithm pipeline,
 * calling a masking function for its input,
 * and delivering the results to its output.
 * @author zinal
 */
public class StepFunction extends StepBase {

    private final MaskingFunction function;

    public StepFunction(String name, StepGroup owner, MaskingFunction function) {
        super(name, owner);
        this.function = function;
    }

    public MaskingFunction getFunction() {
        return function;
    }


    @Override
    public StepType getType() {
        return StepType.Function;
    }

    @Override
    public boolean isIterable() {
        return function.isIterable();
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
        final StepFunction other = (StepFunction) obj;
        return this.function == other.function;
    }

}
