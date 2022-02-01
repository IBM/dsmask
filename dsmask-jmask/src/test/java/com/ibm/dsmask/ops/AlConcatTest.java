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
import com.ibm.dsmask.beans.MskFunc;
import com.ibm.dsmask.impl.XVector;

/**
 *
 * @author zinal
 */
public class AlConcatTest {

    public AlConcatTest() {
    }

    /**
     * Test of exec method, of class AlConcat.
     */
    @Test
    public void testExec() {
        XVector in = new XVector(3);
        in.values[0] = "Пагосян";
        in.values[1] = "Гагик";
        in.values[2] = "Спартакович";
        XVector out = null;
        AlConcat instance = new AlConcat(new MskFunc());
        out = instance.exec(in, out);
        assertEquals(1, out.values.length);
        assertEquals("Пагосян Гагик Спартакович", out.values[0]);
    }

}
