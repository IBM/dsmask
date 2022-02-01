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

/**
 * A single step in a masking algorithm pipeline.
 * @author zinal
 */
public class MaskingStep {

    public static final String BASE_NAME = "$";

    private String name;
    private MaskingFunction function;
    private final List<Ref> references = new ArrayList<>();
    private MaskingPredicate predicate;
    private MaskingUniq uniqCheck;

    private transient int identifier = -1;

    public MaskingStep() {
        this.name = Utils.NONE;
    }

    public MaskingStep(String name, MaskingFunction function) {
        this.name = Utils.lower(name);
        this.function = function;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = Utils.lower(name);
    }

    public MaskingFunction getFunction() {
        return function;
    }

    public void setFunction(MaskingFunction function) {
        this.function = function;
    }

    public List<Ref> getReferences() {
        return references;
    }

    public void setReferences(List<Ref> refs) {
        this.references.clear();
        if (refs!=null)
            this.references.addAll(refs);
    }

    public void addReference(Ref ref) {
        references.add(ref);
    }

    public void addReference(int position) {
        references.add(new Ref(position));
    }

    public void addReference(String name, int position) {
        references.add(new Ref(name, position));
    }

    public boolean isValid() {
        return name.length() > 0
                && function!=null
                && function.isValid();
    }

    public int getIdentifier() {
        return identifier;
    }

    public void setIdentifier(int identifier) {
        this.identifier = identifier;
    }

    public MaskingPredicate getPredicate() {
        return predicate;
    }

    public void setPredicate(MaskingPredicate predicate) {
        this.predicate = predicate;
    }

    public MaskingUniq getUniqCheck() {
        return uniqCheck;
    }

    public void setUniqCheck(MaskingUniq uniqCheck) {
        this.uniqCheck = uniqCheck;
    }

    /**
     * Reference to an already computed value.
     * Starting position number is 1, for consistency with Lua scripts.
     * Normally position is taken from the previous vector (name==null).
     * It can also be taken from any of the preceeding steps (name!=null).
     * There is a pre-defined name "$", which always refers to the original
     * set of rule arguments (input fields).
     */
    public static class Ref {
        private String name;
        private int position;

        public Ref() {
            this.name = "";
            this.position = 1;
        }

        public Ref(int position) {
            checkPosition(position);
            this.name = "";
            this.position = position;
        }

        public Ref(String name, int position) {
            checkPosition(position);
            name = Utils.lower(name);
            this.name = (name.length()==0) ? null : name;
            this.position = position;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = Utils.lower(name);
        }

        public int getPosition() {
            return position;
        }

        public void setPosition(int position) {
            checkPosition(position);
            this.position = position;
        }

        private void checkPosition(int position) {
            if (position<1 || position>1000)
                throw new IllegalArgumentException("Bad position: " + position);
        }
    } // class Ref

}
