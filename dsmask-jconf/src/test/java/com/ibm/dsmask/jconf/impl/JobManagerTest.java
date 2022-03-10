/*
 * Copyright (c) IBM Corp. 2018, 2022.
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
package com.ibm.dsmask.jconf.impl;

import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author zinal
 */
public class JobManagerTest {

    @Test
    public void test1() {
        String input, output;
        input =  "AAA.BBB";
        output = JobManager.safeInvocation(input);
        Assert.assertEquals("AAA-BBB", output);
        input =  "AaA.BBB";
        output = JobManager.safeInvocation(input);
        Assert.assertEquals("AaA-BBB", output);
        input =  "A$A.B$B";
        output = JobManager.safeInvocation(input);
        Assert.assertEquals("A_A-B_B", output);
        input =  "A#A.B$B";
        output = JobManager.safeInvocation(input);
        Assert.assertEquals("A_A-B_B", output);
    }

}
