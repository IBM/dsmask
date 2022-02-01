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
package net.dsmask.model.any;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Masking operation over a set of fields of a single table.
 * Associates the specific subset of input and output fields
 * to the masking rule's inputs and outputs.
 * @author zinal
 */
public class MaskingOperation {

    private final AnyRule rule;
    private final List<AnyField> inputs = new ArrayList<>();
    private final List<AnyField> outputs = new ArrayList<>();

    public MaskingOperation(AnyRule rule) {
        this.rule = rule;
    }

    public AnyRule getRule() {
        return rule;
    }

    public List<AnyField> getInputs() {
        return inputs;
    }

    public List<AnyField> getOutputs() {
        return outputs;
    }

    /**
     * Check whether the field is handled by this masking operation.
     * @param field The field definition object
     * @return true, if the field is handled, and false otherwise.
     */
    public final boolean isFieldHandled(AnyField field) {
        for (AnyField fi : outputs) {
            if (fi == field)
                return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + Objects.hashCode(this.rule);
        hash = 97 * hash + Objects.hashCode(this.inputs);
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
        final MaskingOperation other = (MaskingOperation) obj;
        if (!Objects.equals(this.rule, other.rule)) {
            return false;
        }
        if (!Objects.equals(this.inputs, other.inputs)) {
            return false;
        }
        if (!Objects.equals(this.outputs, other.outputs)) {
            return false;
        }
        return true;
    }

}
