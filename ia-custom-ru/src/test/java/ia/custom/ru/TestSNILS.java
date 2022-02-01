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

import java.math.BigDecimal;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author zinal
 */
public class TestSNILS {

    @Test
    public void test() {
        String inExpected = "11223344595";
        int[] inArray = new int[] {
            1, 1, 2, 2, 3, 3, 4, 4, 5, 9, 5
        };
        // Проверка строкового варианта
        String in = "112-233-445 95";
        String norm = SNILS.normalize(in);
        Assert.assertEquals(inExpected, norm);
        SNILS snils = new SNILS();
        int[] digits = snils.stringToDigits(norm);
        Assert.assertEquals(norm.length(), digits.length);
        Assert.assertArrayEquals(inArray, digits);
        int[] ctl = snils.getControl(digits);
        Assert.assertEquals(digits[digits.length - 2], ctl[0]);
        Assert.assertEquals(digits[digits.length - 1], ctl[1]);
        // Проверка числового варианта
        Long inLong = 11223344595L;
        norm = DcsUtil.extractDigits(inLong);
        Assert.assertEquals(inExpected, norm);
        BigDecimal inDec = new BigDecimal("11223344595.0");
        norm = DcsUtil.extractDigits(inDec);
        Assert.assertEquals(inExpected, norm);
    }

}
