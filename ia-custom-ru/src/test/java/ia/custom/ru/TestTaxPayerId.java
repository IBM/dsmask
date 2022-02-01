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
public class TestTaxPayerId {

    @Test
    public void test12() {
        String inExpected = "690504234932";
        int[] inArray = new int[] {
            6, 9, 0, 5, 0, 4, 2, 3, 4, 9, 3, 2
        };
        // Проверка строкового варианта
        String in = "\t69 050 423 4932 ";
        String norm = DcsUtil.extractDigits(in);
        Assert.assertEquals(inExpected, norm);
        TaxPayerId tpi = new TaxPayerId();
        int[] digits = tpi.stringToDigits(norm);
        Assert.assertEquals(norm.length(), digits.length);
        Assert.assertArrayEquals(inArray, digits);
        int c11 = TaxPayerId.getChecksum(digits, TaxPayerId.N11);
        int c12 = TaxPayerId.getChecksum(digits, TaxPayerId.N12);
        Assert.assertEquals(digits[digits.length - 2], c11);
        Assert.assertEquals(digits[digits.length - 1], c12);
        // Проверка числового варианта
        Long inLong = 690504234932L;
        norm = DcsUtil.extractDigits(inLong);
        Assert.assertEquals(inExpected, norm);
        BigDecimal inDec = new BigDecimal("690504234932.0");
        norm = DcsUtil.extractDigits(inDec);
        Assert.assertEquals(inExpected, norm);
    }

    @Test
    public void test10() {
        int[] inArray = new int[] {
            7, 7, 0, 5, 0, 4, 1, 8, 6, 6
        };
        String in = " 77 050 418 66\t";
        String norm = DcsUtil.extractDigits(in);
        Assert.assertEquals("7705041866", norm);
        TaxPayerId tpi = new TaxPayerId();
        int[] digits = tpi.stringToDigits(norm);
        Assert.assertEquals(norm.length(), digits.length);
        Assert.assertArrayEquals(inArray, digits);
        int c10 = TaxPayerId.getChecksum(digits, TaxPayerId.N10);
        Assert.assertEquals(digits[digits.length - 1], c10);
    }

    @Test
    public void testFull() {
        String in1 = "631629095237";
        boolean res1 = new TaxPayerId().matchValue(in1);
        Assert.assertEquals(true, res1);
        String in2 = "631695237";
        boolean res2 = new TaxPayerId().matchValue(in2);
        Assert.assertEquals(false, res2);
        String in3 = "631629095239";
        boolean res3 = new TaxPayerId().matchValue(in3);
        Assert.assertEquals(false, res3);
    }

}
