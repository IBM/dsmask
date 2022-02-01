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
import com.ibm.dsmask.impl.XVector;

/**
 *
 * @author zinal
 */
public class AlGroovyTest extends TestResources {

    private MskFunc function1 = null;
    private AlGroovyScript algorithm1 = null;

    private MskFunc function2 = null;
    private AlGroovyScript algorithm2 = null;

    private MskFunc function3 = null;
    private AlGroovyScript algorithm3 = null;

    private MskFunc function4 = null;
    private AlGroovyScript algorithm4 = null;

    @Test
    public void testExec1() {
        XVector in = new XVector(1);
        in.values[0] = "Пагосян Гагик Спартакович";
        XVector out = null;
        out = getAlgorithm1().exec(in, out);
        //System.out.println("FPE: [" + in + "] -> [" + out + "]");
        assertEquals(in.values[0].toString().toUpperCase(),
                out.values[0].toString());
        in.values[0] = null;
        out = getAlgorithm1().exec(in, out);
        assertNull(out.values[0]);
    }

    private MskFunc getFunction1() {
        if (function1 == null) {
            function1 = new MskFunc(11, "Groovy1", FunctionType.GroovyScript,
                    "def invoke(String val) { return val?.toUpperCase(); }");
            getContext().addFunction(function1);
        }
        return function1;
    }

    private AlGroovyScript getAlgorithm1() {
        if (algorithm1==null) {
            algorithm1 = new AlGroovyScript(getServices().getGroovyRunner(),
                    getFunction1());
        }
        return algorithm1;
    }

    @Test
    public void testExec2() {
        XVector in = new XVector(3);
        in.values[0] = "Пагосян";
        in.values[1] = "Гагик";
        in.values[2] = "Спартакович";
        XVector out = null;
        out = getAlgorithm2().exec(in, out);
        assertEquals(1, out.values.length);
        assertEquals("Пагосян Гагик Спартакович", out.values[0]);
    }

    private MskFunc getFunction2() {
        if (function2 == null) {
            function2 = new MskFunc(12, "Groovy2", FunctionType.GroovyScript,
                    "def invoke(String x1, String x2, String x3) { "
                            + "return x1 + ' ' + x2 + ' ' + x3; }");
            getContext().addFunction(function2);
        }
        return function2;
    }

    private AlGroovyScript getAlgorithm2() {
        if (algorithm2==null) {
            algorithm2 = new AlGroovyScript(getServices().getGroovyRunner(),
                    getFunction2());
        }
        return algorithm2;
    }

    @Test
    public void testExec3() {
        XVector in = new XVector(1);
        in.values[0] = "Пагосян Гагик Спартакович";
        XVector out = null;
        out = getAlgorithm3().exec(in, out);
        //System.out.println("FPE: [" + in + "] -> [" + out + "]");
        assertEquals(3, out.values.length);
        assertEquals("Пагосян", out.values[0]);
        assertEquals("Гагик", out.values[1]);
        assertEquals("Спартакович", out.values[2]);
        in.values[0] = null;
        out = getAlgorithm3().exec(in, out);
        assertEquals(1, out.values.length);
        assertNull(out.values[0]);
    }

    private MskFunc getFunction3() {
        if (function3 == null) {
            function3 = new MskFunc(13, "Groovy3", FunctionType.GroovyScript,
                    "def invoke(String val) { "
                            + "return val?.split('[ ]'); }");
            getContext().addFunction(function3);
        }
        return function3;
    }

    private AlGroovyScript getAlgorithm3() {
        if (algorithm3==null) {
            algorithm3 = new AlGroovyScript(getServices().getGroovyRunner(),
                    getFunction3());
        }
        return algorithm3;
    }


    @Test
    public void testExec4() {
        XVector in = new XVector(1);
        XVector out = null;
        in.values[0] = "ABC";
        out = getAlgorithm4().exec(in, out);
        assertEquals(1, out.values.length);
        assertEquals("abc", out.values[0]);
        in.values[0] = "АБВ";
        out = getAlgorithm4().exec(in, out);
        assertEquals(1, out.values.length);
        assertEquals("абв", out.values[0]);
        in.values[0] = 123;
        out = getAlgorithm4().exec(in, out);
        assertEquals(1, out.values.length);
        assertEquals(246, out.values[0]);
        in.values[0] = 500;
        out = getAlgorithm4().exec(in, out);
        assertEquals(1, out.values.length);
        assertEquals(1000, out.values[0]);
    }

    private MskFunc getFunction4() {
        if (function4 == null) {
            function4 = new MskFunc(14, "Groovy4", FunctionType.GroovyScript,
                    "def invoke(String val) { "
                            + "return val?.toLowerCase(); }\n\n"
                            + "def invoke(Integer val) { "
                            + "return (val==null) ? null : (2 * val);  }");
            getContext().addFunction(function4);
        }
        return function4;
    }

    private AlGroovyScript getAlgorithm4() {
        if (algorithm4==null) {
            algorithm4 = new AlGroovyScript(getServices().getGroovyRunner(),
                    getFunction4());
        }
        return algorithm4;
    }
}
