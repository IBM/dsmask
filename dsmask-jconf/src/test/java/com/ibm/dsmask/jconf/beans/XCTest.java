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
package com.ibm.dsmask.jconf.beans;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author zinal
 */
public class XCTest {
    
    public XCTest() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of safeConfigName method, of class Utils.
     */
    @Test
    public void testSafeConfigName() {
        System.out.println("test: xc-safeConfigName");
        assertEquals("a-a", Utils.safeConfigName("a/a"));
        assertEquals("b-b-", Utils.safeConfigName("b\\b/"));
        assertEquals("-c-c", Utils.safeConfigName(":c*c"));
        assertEquals("d-d-", Utils.safeConfigName("d.d."));
        assertEquals("---e-e", Utils.safeConfigName("..\\e\\e"));
    }
    
}
