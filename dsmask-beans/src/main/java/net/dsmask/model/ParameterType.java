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
package net.dsmask.model;

/**
 * Logical data type for a masking algorithm parameter.
 * @author zinal
 */
public enum ParameterType {

    /**
     * Masking key reference (name of a MaskingKey entity)
     */
    Key,

    /**
     * A single line of characters
     */
    Line,

    /**
     * Multiple lines of characters
     */
    Text,

    /**
     * int value
     */
    Integer,

    /**
     * long value
     */
    Long,

    /**
     * double value
     */
    Double,

    /**
     * element of pre-defined values list
     */
    Item;

    /**
     * Convert string code to parameter type object.
     * @param v String code
     * @return parameter type object
     */
    public static ParameterType fromString(String v) {
        if (v==null)
            return Line;
        v = v.trim();
        if (v.length()==0)
            return Line;
        for (ParameterType pt : ParameterType.values()) {
            if (v.equalsIgnoreCase(pt.name()))
                return pt;
        }
        return Line;
    }

}
