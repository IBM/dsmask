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
package com.ibm.dsmask.algo;

import net.dsmask.algo.BasicHasher;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author zinal
 */
public class BasicHasherTest {

    @Test
    public void test() {
        Object[] input1 = new Object[] { "this is a test" };
        Object[] input2 = new Object[] { "this is a test", null };
        Object[] input3 = new Object[] { "this is a test", "again" };

        long output1 = new BasicHasher().calcHash(input1, 0);
        long output1iter1 = new BasicHasher().calcHash(input1, 1);
        Assert.assertNotEquals(output1, output1iter1);

        long output2 = new BasicHasher().calcHash(input2, 0);
        Assert.assertNotEquals(output1, output2);

        long output3 = new BasicHasher().calcHash(input3, 0);
        Assert.assertNotEquals(output1, output3);
        Assert.assertNotEquals(output2, output3);

        long output1pass = new BasicHasher("P@$$w0rd").calcHash(input1, 0);
        Assert.assertNotEquals(output1, output1pass);

        long output3again = new BasicHasher().calcHash(input3, 0);
        Assert.assertEquals(output3, output3again);
    }

}
