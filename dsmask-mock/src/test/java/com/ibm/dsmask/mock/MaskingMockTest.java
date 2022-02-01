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
package com.ibm.dsmask.mock;

import java.util.List;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author zinal
 */
public class MaskingMockTest {

    @Test
    public void test() {
        MaskingMockData kmd;
        List<MaskingMockColumn> columns;
        List<Object[]> values;

        kmd = new MaskingMockData(MaskingMockData.TAB1);
        columns = kmd.getColumns();
        values = kmd.getData();
        Assert.assertTrue(values.size() > 0);
        Assert.assertEquals(columns.size(), values.get(0).length);

        kmd = new MaskingMockData(MaskingMockData.TAB2);
        columns = kmd.getColumns();
        values = kmd.getData();
        Assert.assertTrue(values.size() > 0);
        Assert.assertEquals(columns.size(), values.get(0).length);

        kmd = new MaskingMockData(MaskingMockData.TAB3);
        columns = kmd.getColumns();
        values = kmd.getData();
        Assert.assertTrue(values.size() > 0);
        Assert.assertEquals(columns.size(), values.get(0).length);

        kmd = new MaskingMockData("Goblin");
        columns = kmd.getColumns();
        values = kmd.getData();
        Assert.assertTrue(values.size() > 0);
        Assert.assertEquals(columns.size(), values.get(0).length);
    }

}
