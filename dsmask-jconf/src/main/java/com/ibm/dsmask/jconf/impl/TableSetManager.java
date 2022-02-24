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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;

/**
 * Table set manager implements reading and writing of lists of table
 * names from and to XML files.
 * @author zinal
 */
public class TableSetManager {

    private final File workDir;

    public TableSetManager(File workDir) {
        this.workDir = workDir;
    }

    public TableSetManager(String workDir) {
        this.workDir = new File(workDir);
    }

    public File getWorkDir() {
        return workDir;
    }

    /**
     * List the existing table sets.
     * @return List of table set names
     */
    public List<String> enumTableSets() {
        File[] files = workDir.listFiles();
        if (files==null || files.length==0)
            return Collections.emptyList();
        List<String> retval = new ArrayList<>();
        for (File f : files) {
            if (f.isFile() && f.getName().endsWith(".xml")) {
                String name = f.getName();
                retval.add(name.substring(0, name.length()-4));
            }
        }
        return retval;
    }

    /**
     * Delete the specified table set.
     * @param tableSet Table set name
     * @return true, if deleted, false otherwise
     */
    public boolean deleteTableSet(String tableSet) {
        tableSet = safeName(tableSet);
        if (tableSet.length()==0)
            return false;
        return new File(workDir, tableSet + ".xml") . delete();
    }

    /**
     * Read the table set with the specified name.
     * @param tableSet Table set name
     * @return List of table names.
     * @throws java.lang.Exception
     */
    public List<TableName> readTableSet(String tableSet) throws Exception {
        tableSet = safeName(tableSet);
        if (tableSet.length()==0)
            return Collections.emptyList();
        final Element root = new SAXBuilder()
                .build(new File(workDir, tableSet + ".xml")).getRootElement();
        final List<TableName> retval = new ArrayList<>();
        final List<Element> tables = root.getChildren("table");
        if (tables==null || tables.isEmpty())
            return Collections.emptyList();
        for (Element el : tables) {
            final TableName tn = new TableName (
                    el.getAttributeValue("db"),
                    el.getAttributeValue("name")
            );
            retval.add(tn);
        }
        return retval;
    }

    /**
     * Generate a safe name for a table set (replacing dangerous characters).
     * @param name Input table set name.
     * @return Safe table set name.
     */
    public static String safeName(String name) {
        name = (name==null) ? "" : name.trim();
        name = name.replaceAll("[*$?.:/\\\\]", "-");
        return name;
    }
}
