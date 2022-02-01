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
package com.ibm.dsmask.util;

/**
 * Miscellaneous utility methods which are hard to put elsewhere.
 * @author zinal
 */
public class DsMaskUtil {

    /**
     * Convert the code point to a string representation
     * @param sb String builder to append the code point to, or null to allocate a new one
     * @param cp Code point to be converted
     * @return Same string builder object as passed on input, or a new one if null was passed
     */
    public static StringBuilder appendCodepoint(StringBuilder sb, int cp) {
        if (sb==null)
            sb = new StringBuilder();
        if (Character.isBmpCodePoint(cp)) {
            sb.append((char) cp);
        } else if (Character.isValidCodePoint(cp)) {
            sb.append(Character.highSurrogate(cp));
            sb.append(Character.lowSurrogate(cp));
        } else {
            sb.append('?');
        }
        return sb;
    }

    /**
     * Convert the code point to a string representation
     * @param cp Code point to be converted
     * @return String representation of the code point on input
     */
    public static String fromCodepoint(int cp) {
        return appendCodepoint(null, cp).toString();
    }

    /**
     * Convert the input value to boolean
     * @param val input value
     * @return true or false (default false)
     */
    public static boolean toBoolean(String val) {
        if (val==null || val.length()==0)
            return false;
        char c = val.charAt(0);
        return ('1'==c)
                || ('Y'==c) || ('y'==c)
                || ('T'==c) || ('t'==c);
    }

}
