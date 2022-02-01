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

import com.ibm.dsmask.beans.MskFunc;
import com.ibm.dsmask.impl.XVector;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author zinal
 */
public class AlSplitTest {

    public AlSplitTest() {
    }

    /**
     * Test of exec method, of class AlSplit.
     */
    @Test
    public void testExec() {
        XVector in = new XVector(1);
        in.values[0] = "Пагосян Гагик Спартакович";
        XVector out = null;
        AlSplit instance = new AlSplit(new MskFunc());
        out = instance.exec(in, out);
        assertEquals(3, out.values.length);
        assertEquals("Пагосян", out.values[0]);
        assertEquals("Гагик", out.values[1]);
        assertEquals("Спартакович", out.values[2]);
    }

}
