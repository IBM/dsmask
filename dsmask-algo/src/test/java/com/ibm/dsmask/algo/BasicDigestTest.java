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

import net.dsmask.algo.BasicDigest;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author zinal
 */
public class BasicDigestTest {

    @Test
    public void test() {
        Object[] input1 = new Object[] { "this is a test" };
        Object[] input2 = new Object[] { "this is a test", null };
        Object[] input3 = new Object[] { "this is a test", "again" };

        String output1 = new BasicDigest().calcHex(input1, 0);
        Assert.assertEquals("7d0a8468ed220400c0b8e6f335baa7e070ce880a37"
                + "e2ac5995b9a97b809026de626da636ac7365249bb974c719edf5"
                + "43b52ed286646f437dc7f810cc2068375c", output1);

        String output1iter1 = new BasicDigest().calcHex(input1, 1);
        Assert.assertNotEquals(output1, output1iter1);
        Assert.assertEquals("9420932a8d1ff4d833d3e21ce2ea9dfd4b7084ccde"
                + "7ce74989b31de20024405640b2d2a9d15bb9199926f3fb31937f"
                + "a2663de228814fe021257885fd52cb42ed", output1iter1);

        String output2 = new BasicDigest().calcHex(input2, 0);
        Assert.assertNotEquals(output1, output2);
        Assert.assertEquals("6651519f0e00bc2e7155126f5eeecf64db79cf5787"
                + "509cd42292c87804b135c82aa90850c769fdf73488dda548225c"
                + "725e519ac1877a542f94ad6602cf2c20d1", output2);

        String output3 = new BasicDigest().calcHex(input3, 0);
        Assert.assertNotEquals(output1, output3);
        Assert.assertNotEquals(output2, output3);
        Assert.assertEquals("7a7875fd423c308cabf5aeb1d4c63036b193988a63"
                + "652308823887de5ef3d917217fd9c2b8597f15d4a2ce7c231a12"
                + "42c3be6b3dcf711a734bc282fd3be48c30", output3);

        String output1pass = new BasicDigest(null, "P@$$w0rd").calcHex(input1, 0);
        Assert.assertNotEquals(output1, output1pass);
        Assert.assertEquals("5a93ff0a9156ef221baa9748a03fcd8565272c0cef"
                + "be642679d9eb330b326a515ef1aeaa4d966d4a88b3a802992df0"
                + "d04fea06673ec45bd052b0c5572105036b", output1pass);

        String output3again = new BasicDigest().calcHex(input3, 0);
        Assert.assertEquals(output3, output3again);
    }

}
