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
package com.ibm.dsmask.jconf.impl;

import org.junit.Test;
import org.junit.After;
import org.junit.Before;
import com.ibm.dsmask.jconf.mock.*;
import com.ibm.dsmask.jconf.portage.*;

/**
 *
 * @author zinal
 */
public class ScriptCheckerTest {
    
    private MaskingRuleRegistry maskingRuleRegistry = null;

    @Before
    public void setUp() {
        maskingRuleRegistry = MaskingRuleRegistryMock.load();
    }
    
    @After
    public void tearDown() {
    }

    @Test
    public void groovyMaskingFunctionTest() {
        final GroovyTester groovyTester = new GroovyTester();
        groovyTester.test("def invoke(String aaa) { "
                + "return aaa.toLowerCase(); }", 
                "return 'AbCdEfG'", false);
    }

    @Test
    public void groovyPredicateTest() {
        final GroovyTester groovyTester = new GroovyTester();
        groovyTester.test("return 'Y'.equalsIgnoreCase(input[0])", 
                "return 'y'", true);
    }

    @Test
    public void scriptCheckerTest() throws Exception {
        new ScriptChecker(maskingRuleRegistry) . check();
    }

}
