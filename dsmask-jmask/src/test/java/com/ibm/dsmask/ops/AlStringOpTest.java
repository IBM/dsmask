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
import com.ibm.dsmask.beans.*;
import com.ibm.dsmask.impl.XVector;

/**
 *
 * @author zinal
 */
public class AlStringOpTest {

    private AlSimpleVector op1 = null;
    private AlSimpleVector op2 = null;
    private AlSimpleVector op3 = null;

    public AlStringOpTest() {
    }

    private AlSimpleVector getOp1() {
        if (op1==null) {
            MskFunc mf = new MskFunc(1, "test-StringOp1", FunctionType.StringOp);
            mf.setText("Lower\nTrim\nLPad 10 +");
            op1 = new AlDefaultVector(new AlStringOp(mf));
        }
        return op1;
    }

    private AlSimpleVector getOp2() {
        if (op2==null) {
            MskFunc mf = new MskFunc(1, "test-StringOp2", FunctionType.StringOp);
            mf.setText("Upper\nReplace [0-9]+ x");
            op2 = new AlDefaultVector(new AlStringOp(mf));
        }
        return op2;
    }

    private AlSimpleVector getOp3() {
        if (op3==null) {
            MskFunc mf = new MskFunc(1, "test-StringOp3", FunctionType.StringOp);
            mf.setText("Replace [^\\d] \"\"");
            op3 = new AlDefaultVector(new AlStringOp(mf));
        }
        return op3;
    }

    @Test
    public void testOp1() {
        XVector in = new XVector(1);
        in.values[0] = "  ABCЭЮЯ   ";
        XVector out = null;
        out = getOp1().exec(in, out);
        assertEquals(1, out.values.length);
        assertEquals("++++abcэюя", out.values[0]);
    }

    @Test
    public void testOp2() {
        XVector in = new XVector(1);
        in.values[0] = "abc12,34,567";
        XVector out = null;
        out = getOp2().exec(in, out);
        assertEquals(1, out.values.length);
        assertEquals("ABCx,x,x", out.values[0]);
    }

    @Test
    public void testOp3() {
        XVector in = new XVector(1);
        in.values[0] = "+7(4822)55-18-20";
        XVector out = null;
        out = getOp3().exec(in, out);
        assertEquals(1, out.values.length);
        assertEquals("74822551820", out.values[0]);
    }

}
