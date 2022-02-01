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

import java.util.ArrayList;
import java.util.List;

/**
 * Masking operation.
 * Maps inputs and outputs within the table for a single masking rule.
 * @author zinal
 */
public class MskOp {

    // data from the configuration database
    private int id;
    private MskRule rule;
    private MskTable table;
    private final List<String> inputFields = new ArrayList<>();
    private final List<String> outputFields = new ArrayList<>();
    // mapping of field names to field indexes
    private int[] inputIndexes = null;
    private int[] outputIndexes = null;
    // handled field name prefix
    private String scopePrefix;

    /**
     * Normal constructor for an empty (non-loaded) masking operation
     */
    public MskOp() {
    }

    /**
     * Copy constructor to build a variant of a masking operation
     * for a particular field name prefix
     * @param src  Source (loaded) masking operation
     * @param scopePrefix Current scope prefix to be used
     */
    public MskOp(MskOp src, String scopePrefix) {
        this.id = src.id;
        this.rule = src.rule;
        this.table = src.table;
        if (scopePrefix==null || scopePrefix.length()==0) {
            this.inputFields.addAll(src.inputFields);
            this.outputFields.addAll(src.outputFields);
        } else {
            for (String v : src.inputFields) {
                this.inputFields.add(scopePrefix + v);
            }
            for (String v : src.outputFields) {
                this.outputFields.add(scopePrefix + v);
            }
        }
        this.scopePrefix = scopePrefix;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public MskRule getRule() {
        return rule;
    }

    public void setRule(MskRule rule) {
        this.rule = rule;
    }

    public MskTable getTable() {
        return table;
    }

    public void setTable(MskTable table) {
        this.table = table;
    }

    public List<String> getInputFields() {
        return inputFields;
    }

    public List<String> getOutputFields() {
        return outputFields;
    }

    public int[] getInputIndexes() {
        return inputIndexes;
    }

    public void setInputIndexes(int[] inputIndexes) {
        this.inputIndexes = inputIndexes;
    }

    public int[] getOutputIndexes() {
        return outputIndexes;
    }

    public void setOutputIndexes(int[] outputIndexes) {
        this.outputIndexes = outputIndexes;
    }

    /**
     * Get the rule step at the specified position
     * @param position Position of the step
     * @return Step at the specified position
     */
    public MskStep getStep(int position) {
        return rule.getSteps().get(position);
    }

    public String getScopePrefix() {
        return scopePrefix;
    }

    public void setScopePrefix(String scopePrefix) {
        this.scopePrefix = scopePrefix;
    }

}
