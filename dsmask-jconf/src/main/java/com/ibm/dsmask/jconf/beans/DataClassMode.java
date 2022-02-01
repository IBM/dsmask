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
 * Mode of using a data class
 * @author zinal
 */
public enum DataClassMode {
    Normal("N"),
    Confidential("C"),
    Group("G");

    private final String code;

    private DataClassMode(String code) {
        this.code = code;
    }

    public final String getCode() {
        return code;
    }

    public static boolean isValid(String value) {
        if (value==null)
            return true; // null values are allowed
        value = value.trim().toUpperCase();
        if (value.length()==0)
            return true; // empty values are allowed
        final char xvalue = value.charAt(0);
        for (DataClassMode dcm : DataClassMode.values()) {
            if (dcm.code.charAt(0) == xvalue)
                return true;
        }
        return false;
    }

    public static DataClassMode getMode(String value) {
        if (value==null)
            return Normal;
        value = value.trim().toUpperCase();
        if (value.length()==0)
            return Normal;
        final char xvalue = value.charAt(0);
        for (DataClassMode dcm : values()) {
            if (dcm.code.charAt(0) == xvalue)
                return dcm;
        }
        throw new IllegalArgumentException("Bad data class mode: " + value);
    }
}
