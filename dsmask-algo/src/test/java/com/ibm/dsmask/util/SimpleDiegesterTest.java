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
package com.ibm.dsmask.util;

import net.dsmask.util.SimpleDiegester;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author zinal
 */
public class SimpleDiegesterTest {

    @Test
    public void test1() throws Exception {
        String password = "passw0rd";
        SimpleDiegester sd = new SimpleDiegester(password);
        String request = sd.makeRequest();
        Assert.assertEquals(SimpleDiegester.REQ_LEN, request.length());
        String response1 = sd.makeResponse(request);
        Assert.assertNotNull(response1);
        String response2 = sd.makeResponse(request);
        Assert.assertNotNull(response2);
        String response3 = new SimpleDiegester(password).makeResponse(request);
        Assert.assertNotNull(response3);
        String response4 = new SimpleDiegester("aaa").makeResponse(request);
        Assert.assertNotNull(response4);
        Assert.assertEquals(response1, response2);
        Assert.assertEquals(response1, response3);
        Assert.assertNotEquals(response4, response1);
    }

}
