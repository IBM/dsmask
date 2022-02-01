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
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Masking rule: mapping ordered list of data classes to
 * the masking algorithm.
 * Masking rules can be split by execution context, to allow
 * different rules to be run on same data classes in different
 * situations (e.g. to prepare a mapping table).
 * Masking algorithm is represented by the execution pipeline,
 * which maps the input vector to the output vector through
 * a series of related steps.
 * @author zinal
 */
public class MaskingRule {

    private String name;
    private final Set<String> contexts = new HashSet<>();
    private final List<RuleArgument> arguments = new ArrayList<>();
    private final List<RuleArgument> outputs = new ArrayList<>();
    private final List<MaskingStep> pipeline = new ArrayList<>();

    public MaskingRule() {
        this.name = Utils.NONE;
    }

    public MaskingRule(String name) {
        this.name = Utils.lower(name);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = Utils.lower(name);
    }

    public Set<String> getContexts() {
        return contexts;
    }

    public void setContexts(Collection<String> vals) {
        contexts.clear();
        if (vals!=null) {
            contexts.addAll(vals);
        }
    }

    public List<RuleArgument> getArguments() {
        return arguments;
    }

    public RuleArgument getArgument(int pos) {
        return arguments.get(pos);
    }

    public void setArguments(List<RuleArgument> vals) {
        arguments.clear();
        if (vals!=null) {
            arguments.addAll(vals);
        }
    }

    public void setStrArguments(List<String> vals) {
        arguments.clear();
        if (vals!=null) {
            final List<RuleArgument> args = new ArrayList<>();
            for (String val : vals)
                args.add(new RuleArgument(val));
            arguments.addAll(args);
        }
    }

    public List<RuleArgument> getOutputs() {
        return outputs;
    }

    public RuleArgument getOutput(int pos) {
        return outputs.get(pos);
    }

    public void setOutputs(List<RuleArgument> vals) {
        outputs.clear();
        if (vals!=null) {
            outputs.addAll(vals);
        }
    }

    public void setStrOutputs(List<String> vals) {
        outputs.clear();
        if (vals!=null) {
            final List<RuleArgument> args = new ArrayList<>();
            for (String val : vals)
                args.add(new RuleArgument(val));
            outputs.addAll(args);
        }
    }

    public List<MaskingStep> getPipeline() {
        return pipeline;
    }

    public void setPipeline(List<MaskingStep> steps) {
        pipeline.clear();
        if (steps!=null) {
            pipeline.addAll(steps);
        }
    }

    public void addArgument(String argClasses) {
        arguments.add(new RuleArgument(argClasses));
    }

    public void addOutput(String argClasses) {
        outputs.add(new RuleArgument(argClasses));
    }

    public void addField(String argClasses) {
        arguments.add(new RuleArgument(argClasses));
        outputs.add(new RuleArgument(argClasses));
    }

    public void addStep(MaskingStep step) {
        pipeline.add(step);
    }

    public boolean isValid() {
        if ( arguments.isEmpty()
                || outputs.isEmpty()
                || pipeline.isEmpty() )
            return false;
        for (RuleArgument ra : arguments) {
            if (! ra.isValid())
                return false;
        }
        for (RuleArgument ra : outputs) {
            if (! ra.isValid())
                return false;
        }
        final HashSet<RuleArgument> intersect = new HashSet<>(arguments);
        intersect.retainAll(outputs);
        if ( intersect.isEmpty() )
            return false; // there should be at least one element of intersection
        for (MaskingStep ms : pipeline) {
            if (! ms.isValid())
                return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + Objects.hashCode(this.name);
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
        final MaskingRule other = (MaskingRule) obj;
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        if (!Objects.equals(this.contexts, other.contexts)) {
            return false;
        }
        if (!Objects.equals(this.arguments, other.arguments)) {
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

    @Override
    public String toString() {
        return "MaskingRule{" + name + '}';
    }

}
