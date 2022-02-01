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
package com.ibm.dsmask.jconf.beans;

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

    private MaskingRule maskingRule;
    private final List<FieldInfo> arguments = new ArrayList<>();
    private final List<FieldInfo> outputs = new ArrayList<>();

    public MaskingRule getMaskingRule() {
        return maskingRule;
    }

    public void setMaskingRule(MaskingRule maskingRule) {
        this.maskingRule = maskingRule;
    }

    public List<FieldInfo> getArguments() {
        return arguments;
    }

    public void setArguments(List<FieldInfo> arguments) {
        this.arguments.clear();
        this.arguments.addAll(arguments);
    }

    public List<FieldInfo> getOutputs() {
        return outputs;
    }

    public void setOutputs(List<FieldInfo> outputs) {
        this.outputs.clear();
        this.outputs.addAll(outputs);
    }

    public final boolean isFieldHandled(FieldInfo field) {
        for (FieldInfo fi : outputs) {
            if (fi.equals(field))
                return true;
        }
        return false;
    }

    public final boolean intersects(MaskingOperation tmo) {
        for (FieldInfo fi1 : outputs) {
            for (FieldInfo fi2 : tmo.outputs) {
                if (fi1.equals(fi2))
                    return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 71 * hash + Objects.hashCode(this.maskingRule);
        hash = 71 * hash + Objects.hashCode(this.arguments);
        hash = 71 * hash + Objects.hashCode(this.outputs);
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
        if (!Objects.equals(this.maskingRule, other.maskingRule)) {
            return false;
        }
        if (!Objects.equals(this.arguments, other.arguments)) {
            return false;
        }
        if (!Objects.equals(this.outputs, other.outputs)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "\n\tMaskingOperation{" + "rule=" + maskingRule
                + ", args=" + arguments + ", outs=" + outputs + '}';
    }

}
