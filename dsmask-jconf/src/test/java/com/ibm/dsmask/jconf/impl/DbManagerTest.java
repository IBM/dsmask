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
import com.ibm.dsmask.jconf.mock.*;
import java.io.File;

/**
 *
 * @author zinal
 */
public class DbManagerTest {

    private DataClassRegistry dataClassRegistry = null;
    private MaskingRuleRegistry maskingRuleRegistry = null;

    public DbManagerTest() {
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

    /**
     * Test of save method, of class DbManager.
     */
    @Test
    public void testMultiSave() throws Exception {
        File path = new File(new File(".."), "rules-testsuite");
        File filePath = new File(path, "dsmask-testsuite");
        final String fname = filePath.getAbsolutePath();
        System.out.println("Saving configuration as " + fname);
        try (DbManager manager = new DbManager()) {
            DbUtils.deleteFiles(fname);
            manager.create(fname);
            manager.saveKeys(maskingRuleRegistry.retrieveKeys());
            manager.save(RuleSelectorData.
                    profilePrimitive(RuleSelectorData.tablePrimitive(),
                            maskingRuleRegistry));
            manager.save(RuleSelectorData.
                    profileGrouping(RuleSelectorData.tableGrouping(),
                            maskingRuleRegistry));
            manager.close();
            manager.open(fname);
            manager.save(RuleSelectorData.
                    profileOverlaps(RuleSelectorData.tableOverlaps(),
                            maskingRuleRegistry));
            manager.close();
        }
    }

}
