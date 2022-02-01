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

/**
 * Optional uniqueness test for a masking step.
 * Includes the uniqueness map id (to be checked against),
 * optional vector of zero-based input and output values indexes
 * (if none all values are used).
 */
public class MaskingUniq {

    // uniqueness group name
    private String provider;
    // input column positions, zero-based
    private int[] inputPositions;
    // output column positions, zero-based
    private int[] outputPositions;

    public MaskingUniq(String name) {
        this.provider = name;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public int[] getInputPositions() {
        return inputPositions;
    }

    public void setInputPositions(int[] inputPositions) {
        this.inputPositions = inputPositions;
    }

    public int[] getOutputPositions() {
        return outputPositions;
    }

    public void setOutputPositions(int[] outputPositions) {
        this.outputPositions = outputPositions;
    }

}
