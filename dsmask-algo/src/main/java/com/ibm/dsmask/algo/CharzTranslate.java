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
package com.ibm.dsmask.algo;

import com.ibm.dsmask.util.DsMaskUtil;


/**
 * Algorithm to perform character translation according
 * to the translation table specified.
 * @author zinal
 */
public class CharzTranslate {

    private final CharzTable table;

    /**
     * Construct an algorithm instance based on the translation table object.
     * @param table Character translation table
     */
    public CharzTranslate(CharzTable table) {
        this.table = table;
    }

    public CharzTable getTable() {
        return table;
    }

    /**
     * Translate a single character.
     * @param source Input character
     * @return Translated character
     */
    public int translate(int source) {
        return table.translate(source);
    }

    /**
     * Translate a single character.
     * @param source Input character
     * @return Translated character
     */
    public char translate(char source) {
        return (char) table.translate((int)source);
    }

    /**
     * Translate a character sequence.
     * @param cs Input character sequence.
     * @return Output string
     */
    public String translate(CharSequence cs) {
        if (cs==null)
            return null;
        return translate(cs, null).toString();
    }

    /**
     * Translate a character sequence.
     * @param cs Input character sequence.
     * @param output Output buffer for translated characters
     * @return The resulting output buffer
     */
    public StringBuilder translate(CharSequence cs, StringBuilder output) {
        if (cs==null)
            return output;
        final StringBuilder sb = (output == null) ? new StringBuilder() : output;
        cs.codePoints().forEach(src -> {
            DsMaskUtil.appendCodepoint(sb, table.translate(src));
        });
        return sb;
    }

}
