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

import java.util.Arrays;
import java.util.Objects;

/**
 * Optional uniqueness test for a masking item.
 * Includes the uniqueness map id (to be checked against),
 * and optional vectors of input and output values indexes
 * (if none all input and/or output values are used).
 * @author zinal
 */
public class UniqCheck {

    // uniqueness group name
    private final String provider;
    // input column positions, starting from 1
    private int[] inputPositions;
    // output column positions, starting from 1
    private int[] outputPositions;

    public UniqCheck(String provider) {
        this.provider = provider;
    }

    public String getProvider() {
        return provider;
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

    public boolean hasInputPositions() {
        return inputPositions!=null && inputPositions.length > 0;
    }

    public boolean hasOutputPositions() {
        return outputPositions!=null && outputPositions.length > 0;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 79 * hash + Objects.hashCode(this.provider);
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
        final UniqCheck other = (UniqCheck) obj;
        if (!Objects.equals(this.provider, other.provider)) {
            return false;
        }
        if (!Arrays.equals(this.inputPositions, other.inputPositions)) {
            return false;
        }
        if (!Arrays.equals(this.outputPositions, other.outputPositions)) {
            return false;
        }
        return true;
    }

}
