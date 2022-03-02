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
package net.dsmask.algo;

import java.util.HashMap;

/**
 * Transliteration of Cyrillic characters to Latin characters
 * based on similar pronunciation for the Russian language.
 * @author zinal
 */
public class CyrillicTranslit {

    private static final HashMap<Character, String> HM =
            new HashMap<Character, String>();

    static {
        HM.put('а', "a");
        HM.put('А', "A");
        HM.put('б', "b");
        HM.put('Б', "B");
        HM.put('в', "v");
        HM.put('В', "V");
        HM.put('г', "g");
        HM.put('Г', "G");
        HM.put('д', "d");
        HM.put('Д', "D");
        HM.put('е', "e");
        HM.put('Е', "E");
        HM.put('ё', "iyo");
        HM.put('Ё', "Iyo");
        HM.put('ж', "zh");
        HM.put('Ж', "Zh");
        HM.put('з', "z");
        HM.put('З', "Z");
        HM.put('и', "i");
        HM.put('И', "I");
        HM.put('й', "y");
        HM.put('Й', "Y");
        HM.put('к', "k");
        HM.put('К', "K");
        HM.put('л', "l");
        HM.put('Л', "L");
        HM.put('м', "m");
        HM.put('М', "M");
        HM.put('н', "n");
        HM.put('Н', "N");
        HM.put('о', "o");
        HM.put('О', "O");
        HM.put('п', "p");
        HM.put('П', "P");
        HM.put('р', "r");
        HM.put('Р', "R");
        HM.put('с', "s");
        HM.put('С', "S");
        HM.put('т', "t");
        HM.put('Т', "T");
        HM.put('у', "u");
        HM.put('У', "U");
        HM.put('ф', "f");
        HM.put('Ф', "F");
        HM.put('х', "h");
        HM.put('Х', "h");
        HM.put('ц', "ts");
        HM.put('Ц', "Ts");
        HM.put('ч', "ch");
        HM.put('Ч', "Ch");
        HM.put('ш', "sh");
        HM.put('Ш', "Sh");
        HM.put('щ', "stch");
        HM.put('Щ', "Stch");
        HM.put('ъ', "'");
        HM.put('Ъ', "'");
        HM.put('ы', "yi");
        HM.put('Ы', "Yi");
        HM.put('ь', "'");
        HM.put('Ь', "'");
        HM.put('э', "e");
        HM.put('Э', "E");
        HM.put('ю', "ju");
        HM.put('Ю', "Ju");
        HM.put('я', "ja");
        HM.put('Я', "Ja");
    }

    public static String map(String arg) {
        if (arg == null) {
            return null;
        }
        final StringBuilder sb = new StringBuilder();
        for (char c : arg.toCharArray()) {
            String v = HM.get(c);
            if (v == null) {
                sb.append(c);
            } else {
                sb.append(v);
            }
        }
        return sb.toString();
    }

}
