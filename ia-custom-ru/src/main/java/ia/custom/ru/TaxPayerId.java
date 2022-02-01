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
 * ИНН.
 *
 * @author zinal
 */
public class TaxPayerId implements ValueBasedClassifier {

    //                               0  1  2  3  4  5  6  7  8  9  10
    public static final int[] N10 = {2, 4,10, 3, 5, 9, 4, 6, 8};
    public static final int[] N11 = {7, 2, 4,10, 3, 5, 9, 4, 6, 8};
    public static final int[] N12 = {3, 7, 2, 4,10, 3, 5, 9, 4, 6, 8};

    private final Pattern innPattern = Pattern.compile("\\d{10}|\\d{12}");

    @Override
    public boolean matchValue(Object value) {
        if (value==null)
            return false;
        String strValue = DcsUtil.extractDigits(value);
        if (!innPattern.matcher(strValue).matches())
            return false; // Не соответствует формату
        int[] inn = stringToDigits(strValue);
        switch (inn.length) {
            case 12:
                int n11 = getChecksum(inn, N11);
                int n12 = getChecksum(inn, N12);
                return inn[inn.length - 1] == n12 && inn[inn.length - 2] == n11;
            case 10:
                int n = getChecksum(inn, N10);
                return inn[inn.length - 1] == n;
            default: // На практике срабатывать не должно
                return false;
        }
    }

    public static int getChecksum(int[] digits, int[] multipliers) {
        int checksum = 0;
        for (int i = 0; i < multipliers.length; i++) {
            checksum += digits[i] * multipliers[i];
        }
        checksum = checksum % 11;
        if (checksum > 9)
            checksum = 0;
        return checksum;
    }

    // Сокращаем выделение памяти при работе stringToDigits().
    private final int[] work10 = new int[10];
    private final int[] work12 = new int[12];

    public int[] stringToDigits(String value) {
        int len = value.length();
        int[] digits = work12;
        if (len<work12.length) {
            digits = work10;
            len = work10.length;
        } else if (len>work12.length) {
            len = work12.length;
        }
        for (int i=0; i<len; ++i) {
            if ( i < value.length() )
                digits[i] = Character.getNumericValue(value.charAt(i));
            else
                digits[i] = 0;
        }
        return digits;
    }
}
