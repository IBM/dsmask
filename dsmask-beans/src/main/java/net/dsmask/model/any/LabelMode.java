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
package net.dsmask.model.any;

import org.apache.commons.lang3.StringUtils;

/**
 * Mode of using a data class label.
 * @author zinal
 */
public enum LabelMode {
    /**
     * Regular label for non-confidential data.
     */
    Normal("N"),

    /**
     * Label for confidential data.
     */
    Confidential("C"),

    /**
     * Grouping label for both confidential and non-confidential data.
     */
    Group("G");

    private final String code;

    private LabelMode(String code) {
        this.code = code;
    }

    public final String getCode() {
        return code;
    }

    /**
     * Convert a character label mode code to a LabelMode object
     * @param value Character label
     * @return LabelMode object
     */
    public static LabelMode getMode(String value) {
        if (StringUtils.isBlank(value))
            return Normal;
        final String xvalue = value.trim().toUpperCase();
        if (xvalue.length()==0)
            return Normal;
        for (LabelMode lm : values()) {
            if (lm.code.charAt(0) == xvalue.charAt(0))
                return lm;
        }
        throw new IllegalArgumentException("Bad data class mode: " + value);
    }

}
