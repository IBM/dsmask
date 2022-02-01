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
 * Наименование организации, выдавшей документ, идентифицирующий
 * личность гражданина Российской Федерации.
 * 
 * @author zinal
 */
public class PassportAuthority implements ValueBasedClassifier {

    private transient DcsDict keyWords = null;
    private transient DcsDict words = null;
    private transient DcsDict regions = null;

    @Override
    public boolean matchValue(Object value) {
        String strValue = String.valueOf(value).trim().toLowerCase();
        strValue = strValue.replace(",", " ");
        strValue = strValue.replace(".", " ");
        int counter = 0;
        for (String token : strValue.split(" ")) {
            if (token.equals("")) {
                continue;
            }
            if (getKeyWords().contains(token)) {
                counter += 3;
                continue;
            }
            if (getWords().contains(token)) {
                counter++;
                continue;
            }
            if (getRegions().contains(token)) {
                counter += 2;
                continue;
            }
        }
        return (counter >= 7);
    }

    public DcsDict getKeyWords() {
        if (keyWords==null)
            keyWords = DcsFactory.dictionary("PA_Keys");
        return keyWords;
    }

    public DcsDict getWords() {
        if (words==null)
            words = DcsFactory.dictionary("PA_Words");
        return words;
    }

    public DcsDict getRegions() {
        if (regions==null)
            regions = DcsFactory.dictionary("PA_Regions");
        return regions;
    }

}
