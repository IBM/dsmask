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
 * Названия профессий.
 * @author zinal
 */
public class Professions implements ValueBasedClassifier {

    private transient DcsDict professions = null;

    @Override
    public boolean matchValue(Object value) {
        if (value==null)
            return false;
        return getProfessions().contains(value.toString());
    }

    private DcsDict getProfessions() {
        if (professions == null)
            professions = DcsFactory.dictionary("Professions");
        return professions;
    }

}
