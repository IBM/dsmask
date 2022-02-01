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

import org.junit.Assert;
import org.junit.Test;
import com.ibm.dsmask.beans.*;
import com.ibm.dsmask.mock.TestResources;

/**
 *
 * @author zinal
 */
public class AlDateOpTest extends TestResources {

    /*
      Checked values depend on the key mapping!
      KEY dateop-test -> "ieZahch4 Eepoa7ee ungam5Lu"
        defined in TestResources.getContext()
    */

    public static final String OPTIONS_DATEOP =
            "KEY dateop-test\n";

    private MskFunc func_DateOp = null;
    private AlDateOp algo_DateOp = null;

    private MskFunc getFunc_DateOp() {
        if (func_DateOp == null) {
            func_DateOp = new MskFunc(10, "date-simple", FunctionType.DateOp, OPTIONS_DATEOP);
            getContext().addFunction(func_DateOp);
        }
        return func_DateOp;
    }

    private AlDateOp getAlgo_DateOp() {
        if (algo_DateOp==null) {
            algo_DateOp = new AlDateOp(getKeeper(), getFunc_DateOp());
        }
        return algo_DateOp;
    }

    @Test
    public void testDateOp() {
        final String src1 = "1973-06-18";
        final String dst1 = getAlgo_DateOp().exec(src1).toString();
        Assert.assertNotNull(dst1);
        Assert.assertEquals(src1.length(), dst1.length());
        Assert.assertNotEquals(src1, dst1);
    }

}
