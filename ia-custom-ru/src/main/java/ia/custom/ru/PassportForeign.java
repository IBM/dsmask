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
import java.util.regex.Pattern;

/**
 * Номер заграничного паспорта, Россия.
 * @author zinal
 */
public class PassportForeign implements ValueBasedClassifier {

    private final Pattern pattern = Pattern.compile("^\\d{2}\\s*\\d{7}$");

    @Override
    public boolean matchValue(Object value) {
        String strValue = String.valueOf(value).trim();
        return pattern.matcher(strValue).matches();
    }

}
