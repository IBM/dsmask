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
package com.ibm.dsmask.ops;

import org.junit.Test;
import static org.junit.Assert.*;
import com.ibm.dsmask.mock.*;
import com.ibm.dsmask.beans.*;

/**
 *
 * @author zinal
 */
public class AlFpeTest extends TestResources {

    public static final String OPTIONS_FPE1 =
            "KEY default\n"
            + "CLASS build-test\n";

    public static final String OPTIONS_FPE2 =
            "KEY default\n"
            + "CLASS build-test\n"
            + "SKIP-BEFORE 4\n"
            + "SKIP-AFTER 3";

    private MskFunc function1 = null;
    private AlFPE algorithm1 = null;
    private MskFunc function2 = null;
    private AlFPE algorithm2 = null;

    public AlFpeTest() {
    }

    /**
     * Test of exec method, of class AlFPE.
     */
    @Test
    public void testExec1() {
        Object in = "Пагосян Гагик Спартакович";
        Object out;
        out = getAlgorithm1().exec(in);
        //System.out.println("FPE: [" + in + "] -> [" + out + "]");
        assertEquals("Эеяжггп Хытяс Фюсщъмэййож", out);

        in = "3704 451985";
        out = getAlgorithm1().exec(in);
        //System.out.println("FPE [" + in + "] -> [" + out + "]");
        assertEquals("9403 385413", out);

        in = "small letters AND BIG LETTERS по русски И БОЛЬШЕ";
        out = getAlgorithm1().exec(in);
        //System.out.println("FPE [" + in + "] -> [" + out + "]");
        assertEquals("hfrny eexdbof HBH DDN PTRKXUO лл гхюасщ Л ЙШХШБД", out);

        in = "abcdefghijklmnopqrstuvwxyz ABCDEFGHIJKLMNOPQRSTUVWXYZ\n"
                + "АБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯ\n"
                + "абвгдеёжзийклмнопрстуфхцчшщъыьэюя\n"
                + "0123456789 ;-+$";
        out = getAlgorithm1().exec(in);
        //System.out.println("FPE [" + in + "] -> [" + out + "]");
        assertEquals(out,
                "ddmnchirgaddscmkvedbxvyosz JPEILBFOQJBBBACSCZIKOMJGBB\n"
                + "УВЧЛГСУОВРМКЭСРИГРЬНЯАЭБМЧТГКПДХЯ\n"
                + "цылцузжььюжфдзфузъэиёийгсчцрощоьи\n"
                + "8634540603 ;-+$");
    }

    /**
     * Test of exec method, of class AlFPE.
     */
    @Test
    public void testExec2() {
        Object in = "1234 567890 abc def";
        Object out;
        out = getAlgorithm2().exec(in);
        //System.out.println("FPE: [" + in + "] -> [" + out + "]");
        assertEquals("1234 324020 koc def", out);
    }

    private MskFunc getFunction1() {
        if (function1 == null) {
            function1 = new MskFunc(10, "FPE1", FunctionType.FPE, OPTIONS_FPE1);
            getContext().addFunction(function1);
        }
        return function1;
    }

    private AlFPE getAlgorithm1() {
        if (algorithm1==null) {
            algorithm1 = new AlFPE(getKeeper(), getFunction1());
        }
        return algorithm1;
    }

    private MskFunc getFunction2() {
        if (function2 == null) {
            function2 = new MskFunc(10, "FPE2", FunctionType.FPE, OPTIONS_FPE2);
            getContext().addFunction(function2);
        }
        return function2;
    }

    private AlFPE getAlgorithm2() {
        if (algorithm2==null) {
            algorithm2 = new AlFPE(getKeeper(), getFunction2());
        }
        return algorithm2;
    }

}
