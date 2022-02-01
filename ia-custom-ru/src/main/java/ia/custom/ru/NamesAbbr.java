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
package ia.custom.ru;

import com.ibm.infosphere.classification.ValueBasedClassifier;

/**
 * Фамилия И.О.
 * @author zinal
 */
public class NamesAbbr extends NamesBase implements ValueBasedClassifier {

    private static final int DOT = ".".codePointAt(0);

    @Override
    public boolean matchValue(Object value) {
        if (value==null)
            return false;
        boolean nl = false, na = false;
        for (String item : extract(value)) {
            if (item.length()==0)
                continue;
            if ( getNamesLast().containsDirect(item) ) {
                nl = true;
            } else if ( ! getNamesItems().containsDirect(item) ) {
                if (item.length() < 10) {
                    int[] letters = item.codePoints().toArray();
                    // Либо одна буква, либо буква+точка
                    if (Character.isLetter(letters[0])
                            && ( (letters.length==1)
                                || (letters[letters.length - 1] == DOT) )) {
                        na = true;
                        continue;
                    }
                }
                return false;
            }
        }
        return (nl && na);
    }

}
