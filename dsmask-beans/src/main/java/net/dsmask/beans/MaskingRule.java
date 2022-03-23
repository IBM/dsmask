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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import net.dsmask.model.*;

/**
 * Masking rule: mapping ordered list of data classes to
 * the masking procedure.
 * Masking procedure is represented by the execution pipeline,
 * which maps the input vector to the output vector through
 * a series of related steps.
 * Masking rules can be split by execution context, to allow
 * different rules to be run on same data classes in different
 * situations (e.g. to prepare a mapping table).
 * @author zinal
 */
public class MaskingRule extends EntityBase implements AnyRule {

    private final Set<String> contexts = new HashSet<>();
    private final List<MetaReference> inputs = new ArrayList<>();
    private final List<MetaReference> outputs = new ArrayList<>();
    private final ItemBlock pipeline = new ItemBlock();

    public MaskingRule(String name) {
        super(EntityType.Rule, name);
    }

    public Set<String> getContexts() {
        return contexts;
    }

    @Override
    public List<MetaReference> getInputs() {
        return inputs;
    }

    @Override
    public List<MetaReference> getOutputs() {
        return outputs;
    }

    public MaskingRule addInput(MetaReference mr) {
        if (mr==null)
            throw new NullPointerException();
        inputs.add(mr);
        return this;
    }

    public MaskingRule addOutput(MetaReference mr) {
        if (mr==null)
            throw new NullPointerException();
        outputs.add(mr);
        return this;
    }

    public ItemBlock getPipeline() {
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
        final MaskingRule other = (MaskingRule) obj;
        if (!Objects.equals(this.contexts, other.contexts)) {
            return false;
        }
        if (!Objects.equals(this.inputs, other.inputs)) {
            return false;
        }
        if (!Objects.equals(this.outputs, other.outputs)) {
            return false;
        }
        if (!Objects.equals(this.pipeline, other.pipeline)) {
            return false;
        }
        return true;
    }

}
