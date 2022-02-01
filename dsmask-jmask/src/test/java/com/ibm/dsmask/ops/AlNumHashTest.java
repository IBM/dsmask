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
public class AlNumHashTest extends TestResources {

    public static final String OPTIONS1 =
            "KEY default\n"
            + "FROM 510\n"
            + "TO 511\n";

    public static final String OPTIONS2 =
            "KEY default\n"
            + "FROM 0\n"
            + "TO 100\n";

    private MskFunc function1 = null;
    private AlNumberHash algorithm1 = null;
    private MskFunc function2 = null;
    private AlNumberHash algorithm2 = null;

    public AlNumHashTest() {
    }

    /**
     * Test of exec method, of class AlFPE.
     */
    @Test
    public void testExec1() {
        XVector in = new XVector(1);
        XVector out = null;

        for ( int i=0; i<1000; ++i ) {
            in.values[0] = i;
            out = getAlgorithm1().exec(in, out);
            assertNotNull(out.values[0]);
            assertTrue("Long output type", out.values[0] instanceof Long);
            Long value = (Long) out.values[0];
            assertTrue("Output value within range: " + value,
                    (value >= 510) && (value <= 511));
        }
    }

    /**
     * Test of exec method, of class AlFPE.
     */
    @Test
    public void testExec2() {
        XVector in = new XVector(1);
        XVector out = null;

        for ( int i=0; i<1000; ++i ) {
            in.values[0] = i;
            out = getAlgorithm2().exec(in, out);
            assertNotNull(out.values[0]);
            assertTrue("Long output type", out.values[0] instanceof Long);
            Long value = (Long) out.values[0];
            assertTrue("Output value within range: " + value,
                    (value >= 0) && (value <= 100));
        }
    }

    private MskFunc getFunction1() {
        if (function1 == null) {
            function1 = new MskFunc(11, "T1", FunctionType.NumberHash, OPTIONS1);
            getContext().addFunction(function1);
        }
        return function1;
    }

    private AlNumberHash getAlgorithm1() {
        if (algorithm1==null) {
            algorithm1 = new AlNumberHash(getKeeper(), getFunction1());
        }
        return algorithm1;
    }

    private MskFunc getFunction2() {
        if (function2 == null) {
            function2 = new MskFunc(12, "T2", FunctionType.NumberHash, OPTIONS2);
            getContext().addFunction(function2);
        }
        return function2;
    }

    private AlNumberHash getAlgorithm2() {
        if (algorithm2==null) {
            algorithm2 = new AlNumberHash(getKeeper(), getFunction2());
        }
        return algorithm2;
    }

}
