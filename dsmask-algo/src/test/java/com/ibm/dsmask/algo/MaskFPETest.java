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
package com.ibm.dsmask.algo;

import net.dsmask.algo.MaskFPE;
import net.dsmask.algo.CharClassSet;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author zinal
 */
public class MaskFPETest extends TestResources {

    private MaskFPE algorithm1 = null;
    private MaskFPE algorithm2 = null;
    private MaskFPE algorithm3 = null;

    public MaskFPETest() {
    }

    @Test
    public void test1Exec1() {
        Object in, out;
        in = "Пагосян Гагик Спартакович";
        out = getAlgorithm1().calculate(in);
        //System.out.println("FPE: [" + in + "] -> [" + out + "]");
        assertEquals("Ьщвншфъ Ъапцй Зыюыыпяэццй", out);
    }

    @Test
    public void test1Exec2() {
        Object in, out;
        in = "3704 451985";
        out = getAlgorithm1().calculate(in);
        //System.out.println("FPE [" + in + "] -> [" + out + "]");
        assertEquals("4623 730177", out);
    }

    @Test
    public void test1Exec3() {
        Object in, out;
        in = "small letters AND BIG LETTERS по русски И БОЛЬШЕ";
        out = getAlgorithm1().calculate(in);
        //System.out.println("FPE [" + in + "] -> [" + out + "]");
        assertEquals("pjvnv eglndxh ESO VME FDAVXNX иё цдмевб В ЧКВКЁЬ", out);
    }

    @Test
    public void test1Exec4() {
        Object in, out;
        in = "abcdefghijklmnopqrstuvwxyz ABCDEFGHIJKLMNOPQRSTUVWXYZ\n"
                + "АБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯ\n"
                + "абвгдеёжзийклмнопрстуфхцчшщъыьэюя\n"
                + "0123456789 ;-+$";
        out = getAlgorithm1().calculate(in);
        //System.out.println("FPE [" + in + "] -> [" + out + "]");
        assertEquals(out,
                "gdzipddtqfaxogowvgfccgpknf FXDDQLYBBECFCALYZDCKWNFBFE\n"
                + "ДСЬМТЗМТЬДРМСМЦТОРУТТЧЯСБЩАСЬЬЛИЁ\n"
                + "зафригэпзижсоршвющктчлхусимпхъюов\n"
                + "4071431805 ;-+$");
    }

    @Test
    public void test2() {
        Object in = "1234 567890 abc def";
        Object out;
        out = getAlgorithm2().calculate(in);
        //System.out.println("FPE: [" + in + "] -> [" + out + "]");
        assertEquals("1234 448323 dcn def", out);
    }

    @Test
    public void test3() {
        Object in = "1234567890123401";
        Object out;
        out = getAlgorithm3().calculate(in);
        assertEquals("1234563903434188", out);
    }

    private MaskFPE getAlgorithm1() {
        if (algorithm1==null) {
            algorithm1 = new MaskFPE();
        }
        return algorithm1;
    }

    private MaskFPE getAlgorithm2() {
        if (algorithm2==null) {
            algorithm2 = new MaskFPE(CharClassSet.DEFAULT_RUSSIAN, "", 4, 3);
        }
        return algorithm2;
    }

    private MaskFPE getAlgorithm3() {
        if (algorithm3==null) {
            algorithm3 = new MaskFPE(CharClassSet.DEFAULT_RUSSIAN, "qazwsx0", 6, 0);
        }
        return algorithm3;
    }

}
