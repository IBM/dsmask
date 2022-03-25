/*
 * Copyright (c) IBM Corp. 2018, 2022.
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

import net.dsmask.algo.MaskFPH;
import net.dsmask.algo.CharClassSet;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author zinal
 */
public class MaskFPHTest extends TestResources {

    private MaskFPH algorithm1 = null;
    private MaskFPH algorithm1bis = null;
    private MaskFPH algorithm2 = null;
    private MaskFPH algorithm3 = null;

    public MaskFPHTest() {
    }

    @Test
    public void test1Exec1() {
        Object in, out;
        in = "Пагосян Гагик Спартакович";
        out = getAlgorithm1().calculate(in);
        // System.out.println("FPH: [" + in + "] -> [" + out + "]");
        assertEquals("Тцздонм Овшжж Нщхоекётжёг", out);
    }

    @Test
    public void test1Exec2() {
        Object in, out;
        in = "3704 451985";
        out = getAlgorithm1().calculate(in);
        // System.out.println("FPE [" + in + "] -> [" + out + "]");
        assertEquals("6259 101124", out);
    }

    @Test
    public void test1Exec3() {
        Object in, out, out2, out3, outbis;
        in = "small letters AND BIG LETTERS по русски И БОЛЬШЕ";
        out = getAlgorithm1().calculate(in);
        out2 = getAlgorithm1().calculate(in, 1); // iteration must provide different result
        out3 = getAlgorithm1().calculate(in, 2); // again must be different
        outbis = getAlgorithm1bis().calculate(in); // different key - different result
        // System.out.println("FPE1 [" + in + "] -> [" + out + "]");
        // System.out.println("FPE2 [" + in + "] -> [" + out2 + "]");
        // System.out.println("FPE3 [" + in + "] -> [" + out3 + "]");
        // System.out.println("FPEB [" + in + "] -> [" + outbis + "]");
        assertEquals("ybcic pfgjnqt FHM NJD XWYJFVW бх арннйп Щ ИХЙЭЦЗ", out);
        assertEquals("jchjr feuwcyf AOD IRM RZDPNWM уа нныттл Ъ ТЙГХОР", out2);
        assertEquals("vjrnm bbenwoe CRY BDW EMOCBOP шш лнмйуз Ъ МЪЪЮДГ", out3);
        assertEquals("fiira hjrlnej AQT BHG MUYGOBY вм цйёцнц Б ЪЖЁЕЛЫ", outbis);
    }

    @Test
    public void test1Exec4() {
        Object in, out;
        in = "abcdefghijklmnopqrstuvwxyz ABCDEFGHIJKLMNOPQRSTUVWXYZ\n"
                + "АБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯ\n"
                + "абвгдеёжзийклмнопрстуфхцчшщъыьэюя\n"
                + "0123456789 ;-+$";
        out = getAlgorithm1().calculate(in);
        // System.out.println("FPE [" + in + "] -> [" + out + "]");
        assertEquals(out,
                "zftzjmyysdodfczvrtffsxovdx ZFFDFEXCFUMCWJKGIWFNKCSYDV\n"
                + "ЮХВХФИЦЯЦЕПДЁЙЫИЭТКГППБЪФФГЫСЧСЖЮ\n"
                + "фочтбрзсллдосрймфуъъвюлюъйешадфйч\n"
                + "1013115145 ;-+$");
    }

    @Test
    public void test2() {
        Object in = "1234 567890 abc def";
        Object out;
        out = getAlgorithm2().calculate(in);
        // System.out.println("FPH: [" + in + "] -> [" + out + "]");
        assertEquals("1234 543567 pzf def", out);
    }

    @Test
    public void test3() {
        Object in = "1234567890123401";
        Object out;
        out = getAlgorithm3().calculate(in);
        // System.out.println("FPH: [" + in + "] -> [" + out + "]");
        assertEquals("1234563916845100", out);
    }

    private MaskFPH getAlgorithm1() {
        if (algorithm1==null) {
            algorithm1 = new MaskFPH();
        }
        return algorithm1;
    }

    private MaskFPH getAlgorithm1bis() {
        if (algorithm1bis==null) {
            algorithm1bis = new MaskFPH(CharClassSet.DEFAULT_RUSSIAN, "КлючОтКисловодска");
        }
        return algorithm1bis;
    }

    private MaskFPH getAlgorithm2() {
        if (algorithm2==null) {
            algorithm2 = new MaskFPH(CharClassSet.DEFAULT_RUSSIAN, "", 4, 3);
        }
        return algorithm2;
    }

    private MaskFPH getAlgorithm3() {
        if (algorithm3==null) {
            algorithm3 = new MaskFPH(CharClassSet.DEFAULT_RUSSIAN, "qazwsx0", 6, 0);
        }
        return algorithm3;
    }

}
