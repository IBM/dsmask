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
 * Наименование организации.
 * @author zinal
 */
public class OrgName implements ValueBasedClassifier {

    private transient DcsDict words = null;

    @Override
    public boolean matchValue(Object value) {
        String strValue = String.valueOf(value).trim().toLowerCase();
        strValue = strValue.replace("[,\\-\t\"\']", " ");
        int matchedCount = 0;
        int otherCount = 0;
        for (String token : strValue.split(" ")) {
            if (token.equals("")) {
                continue;
            }
            if (getWords().contains(token)) {
                ++matchedCount;
                continue;
            }
            if (token.length() > 3)
                ++otherCount;
        }
        return (matchedCount >= 1) && (otherCount >= 1);
    }

    public DcsDict getWords() {
        if (words==null)
            words = DcsFactory.dictionary("Org_Words");
        return words;
    }

}
