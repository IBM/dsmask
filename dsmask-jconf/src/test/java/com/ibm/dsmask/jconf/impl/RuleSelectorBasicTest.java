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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import com.ibm.dsmask.jconf.mock.*;
import com.ibm.dsmask.jconf.beans.*;

/**
 *
 * @author zinal
 */
public class RuleSelectorBasicTest {

    private DataClassRegistry dataClassRegistry = null;
    private MaskingRuleRegistry maskingRuleRegistry = null;

    public RuleSelectorBasicTest() {
    }

    @Before
    public void setUp() {
        dataClassRegistry = new DataClassRegistryMock();
        dataClassRegistry.polulate();
        maskingRuleRegistry = MaskingRuleRegistryMock.load();
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testBasic() {
        // input table
        TableInfo table = RuleSelectorData.tablePrimitive();

        String context = "";

        RuleSelector instance = new RuleSelector();
        instance.setDataClassRegistry(dataClassRegistry);
        instance.setRuleRegistry(maskingRuleRegistry);

        // method execution
        MaskingProfile result = instance.select(table, context);
        // basic checks (mostly redudant, but let them stay here)
        assertNotNull(result);
        assertNotNull(result.getTableInfo());
        assertNotNull(result.getOperations());
        assertEquals(table, result.getTableInfo());

//        System.out.println("OUTPUT: " + result.getOperations());

        // model the expected output (order is not significant)
        MaskingProfile expected = RuleSelectorData.
                profilePrimitive(table, maskingRuleRegistry);
        // we should get what we expect
        assertEquals(expected, result);
    }
}
