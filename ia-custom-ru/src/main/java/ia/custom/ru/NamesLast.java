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
 * Фамилии.
 * @author zinal
 */
public class NamesLast extends NamesBase implements ValueBasedClassifier {

    @Override
    public boolean matchValue(Object value) {
        if (value==null)
            return false;
        boolean found = false;
        for (String item : extract(value)) {
            if (item.length()==0)
                continue;
            if ( getNamesLast().containsDirect(item) ) {
                found = true;
            } else if ( ! getNamesItems().containsDirect(item) ) {
                // Не похоже на имя, и не типичное междомение в середине.
                return false;
            }
        }
        return found;
    }

}
