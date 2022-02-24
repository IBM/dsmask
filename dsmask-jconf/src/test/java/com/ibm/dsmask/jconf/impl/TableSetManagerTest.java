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

import com.ibm.dsmask.jconf.beans.TableName;
import java.io.File;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author zinal
 */
public class TableSetManagerTest {

    private TableSetManager manager = null;

    private TableSetManager grabManager() throws Exception {
        if (manager!=null)
            return manager;
        File path = new File("..");
        path = new File(path, "testsuite");
        path = new File(path, "tablesets");
        path = path.getCanonicalFile();
        manager = new TableSetManager(path);
        System.out.println("Mock table set directory: " + path);
        return manager;
    }

    @Test
    public void test0() throws Exception {
        List<String> tableSets = grabManager().enumTableSets();
        Assert.assertTrue("at least two table sets", tableSets.size() >= 2);
        Assert.assertTrue("table set sample1 exists", tableSets.contains("sample1"));
        Assert.assertTrue("table set sample2 exists", tableSets.contains("sample2"));

        List<TableName> ts1 = grabManager().readTableSet("sample1");
        List<TableName> ts2 = grabManager().readTableSet("sample2");
        Assert.assertTrue("table set sample1 not empty", !ts1.isEmpty());
        Assert.assertTrue("table set sample2 not empty", !ts2.isEmpty());
        Assert.assertEquals("table sets equal", ts1, ts2);
    }

}
