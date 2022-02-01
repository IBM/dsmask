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
package com.ibm.dsmask.beans;

import java.util.List;
import java.util.ArrayList;

/**
 * Step of a masking rule.
 * @author zinal
 */
public class MskStep {

    private final MskRule owner;
    private final int position;
    private int id = -1;
    private String name = null;
    private String predicateText = null;
    private MskFunc function = null;
    private MskUniq uniqCheck = null;
    private final List<MskRef> refs = new ArrayList<>();

    public MskStep(MskRule owner, int position) {
        this.owner = owner;
        this.position = position;
    }

    public MskRule getOwner() {
        return owner;
    }

    public int getPosition() {
        return position;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPredicateText() {
        return predicateText;
    }

    public void setPredicateText(String predicateText) {
        this.predicateText = predicateText;
    }

    public boolean hasPredicate() {
        if (predicateText==null)
            return false;
        if (predicateText.length()==0)
            return false;
        return true;
    }

    public MskFunc getFunction() {
        return function;
    }

    public void setFunction(MskFunc function) {
        this.function = function;
    }

    public String getFunctionName() {
        return (function==null) ? "<null-function>" : function.getName();
    }

    public MskUniq getUniqCheck() {
        return uniqCheck;
    }

    public void setUniqCheck(MskUniq uniqCheck) {
        this.uniqCheck = uniqCheck;
    }

    public List<MskRef> getRefs() {
        return refs;
    }

    @Override
    public String toString() {
        return "MskStep{" + "owner=" + owner + ", position=" + position + ", name=" + name + '}';
    }

}
