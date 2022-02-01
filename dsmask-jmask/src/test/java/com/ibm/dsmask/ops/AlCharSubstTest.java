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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import org.junit.Assert;
import org.junit.Test;
import com.ibm.dsmask.beans.*;
import com.ibm.dsmask.algo.CharzTable;
import com.ibm.dsmask.mock.TestResources;

/**
 *
 * @author zinal
 */
public class AlCharSubstTest extends TestResources {

    public static final String OPTIONS_X2U =
            "TABLE build-test-ge-x2u\n";
    public static final String OPTIONS_U2X =
            "TABLE build-test-ge-u2x\n";

    private MskFunc func_X2U = null;
    private AlCharSubst algo_X2U = null;
    private MskFunc func_U2X = null;
    private AlCharSubst algo_U2X = null;

    private MskFunc getFunc_X2U() {
        if (func_X2U == null) {
            func_X2U = new MskFunc(10, "GE_X2U", FunctionType.CharSubst, OPTIONS_X2U);
            getContext().addFunction(func_X2U);
        }
        return func_X2U;
    }

    private AlCharSubst getAlgo_X2U() {
        if (algo_X2U==null) {
            algo_X2U = new AlCharSubst(getKeeper(), getFunc_X2U());
        }
        return algo_X2U;
    }

    private MskFunc getFunc_U2X() {
        if (func_U2X == null) {
            func_U2X = new MskFunc(10, "GE_U2X", FunctionType.CharSubst, OPTIONS_U2X);
            getContext().addFunction(func_U2X);
        }
        return func_U2X;
    }

    private AlCharSubst getAlgo_U2X() {
        if (algo_U2X==null) {
            algo_U2X = new AlCharSubst(getKeeper(), getFunc_U2X());
        }
        return algo_U2X;
    }

    @Test
    public void testGeorgianSpecial() {
        final String GE_ALPHABET = "აბგდევზთიკლმნოპჟრსტუფქღყშჩცძწჭხჯჰ";
        final String GE_TRANS = "ÀÁÂÃÄÅÆÈÉÊËÌÍÏÐÕÒÓÔÖ×ØÙÚÛÜÝÞßàáãä";
        String trans1 = getAlgo_U2X().exec(GE_ALPHABET).toString();
        Assert.assertEquals(GE_TRANS, trans1);
        String trans2 = getAlgo_X2U().exec(trans1).toString();
        Assert.assertEquals(GE_ALPHABET, trans2);
    }

    @Test
    public void testDumpLoad() throws Exception {
        CharzTable table1 = getAlgo_X2U().getAlgo().getTable();
        //table1.dump(System.out);
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        table1.dump(baos);
        CharzTable table2 = CharzTable.load(new ByteArrayInputStream(baos.toByteArray()));
        Assert.assertEquals(table1.getName(), table2.getName());
        Assert.assertEquals(table1.getRanges(), table2.getRanges());
        Assert.assertEquals(table1, table2);
    }

}
