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
 * JDOM handling support methods.
 * @author zinal
 */
public class JdomHelpers {

    /**
     * Grab the character value in its hex or character form from the corresponding XML attributes
     * @param attr Attribute name base, actual attributes with be attr+"-char" or attr+"-hex"
     * @param el JDOM element to grab the attribute from
     * @return Character value in its string form, or null, if none found or not valid
     */
    public static String getCharVal(String attr, org.jdom2.Element el) {
        String val = el.getAttributeValue(attr + "-char");
        if (val!=null && val.length() > 0)
            return val;
        val = el.getAttributeValue(attr + "-hex");
        if (val==null || val.length()==0)
            return null;
        int codePoint = Integer.parseInt(val, 16);
        if (codePoint < 0)
            codePoint = -codePoint;
        if (Character.isValidCodePoint(codePoint))
            return DsMaskUtil.fromCodepoint(codePoint);
        return null;
    }


}
