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

/**
 * Uniqueness check for a masking step.
 * @author zinal
 */
public class MskUniq {

    // uniqueness group name
    private String provider;
    // input column positions, zero-based
    private int[] inputPositions;
    // output column positions, zero-based
    private int[] outputPositions;

    public String getProvider() {
        if (provider==null)
            return "sys$uniq";
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public int[] getInputPositions() {
        return inputPositions;
    }

    public void setInputPositions(int[] columnIndexes) {
        this.inputPositions = columnIndexes;
    }

    public int[] getOutputPositions() {
        return outputPositions;
    }

    public void setOutputPositions(int[] outputIndexes) {
        this.outputPositions = outputIndexes;
    }

}
