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
 * Полное ФИО.
 * @author zinal
 */
public class NamesFull extends NamesBase implements ValueBasedClassifier {

    @Override
    public boolean matchValue(Object value) {
        if (value==null)
            return false;
        boolean nf = false, nm = false, nl = false;
        for (String item : extract(value)) {
            if (item.length()==0)
                continue;
            if ( getNamesFirst().containsDirect(item) ) {
                nf = true;
            } else if ( getNamesMiddle().containsDirect(item) ) {
                nm = true;
            } else if ( getNamesLast().containsDirect(item) ) {
                nl = true;
            } else if ( ! getNamesItems().containsDirect(item) ) {
                // Не похоже на имя, и не типичное междомение в середине.
                return false;
            }
        }
        int count = 0;
        if (nf) ++count;
        if (nm) ++count;
        if (nl) ++count;
        // Встретилось минимум 2 компонента в любом составе и порядке.
        return (count > 1);
    }

}
