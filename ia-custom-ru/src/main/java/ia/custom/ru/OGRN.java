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
 * ОРГН для юридических лиц и ИП.
 *
 * @author zinal
 */
public class OGRN implements ValueBasedClassifier {

    private final Pattern pattern = Pattern.compile("\\d{13}|\\d{15}");

    @Override
    public boolean matchValue(Object value) {
        if (value==null)
            return false;
        String strValue = DcsUtil.extractDigits(value);
        if (!pattern.matcher(strValue).matches())
            return false; // Не соответствует формату
        long longValue = Long.parseLong(strValue);
        int delimiter = (strValue.length() <= 13) ? 11 : 13;
        int control1 = (int) (longValue % 10L);
        int control2 = (int) ( (longValue/10L) % delimiter ) % 10;
        return (control1 == control2);
    }
}
