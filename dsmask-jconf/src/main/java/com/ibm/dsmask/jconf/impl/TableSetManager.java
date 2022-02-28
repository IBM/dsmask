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

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import com.ibm.dsmask.jconf.beans.TableName;

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
     * Convert XML DOM tree into the list of table names.
     * @param root Root of XML DOM tree
     * @return List of table names.
     */
    public List<TableName> convertTableSet(Element root) {
        final String dbname = root.getAttributeValue("db");
        final List<Element> tables = root.getChildren("item");
        if (tables==null || tables.isEmpty())
            return Collections.emptyList();
        final List<TableName> retval = new ArrayList<>();
        for (Element tab : tables) {
            final TableName tn = new TableName( dbname,
                    tab.getAttributeValue("schema"),
                    tab.getAttributeValue("table") );
            if (tn.isValid())
                retval.add(tn);
        }
        return retval;
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
        return convertTableSet(root);
    }

    /**
     * Convert a table set to XML DOM tree.
     * @param tableSet Table set name
     * @param entries Table set entries
     * @return XML DOM tree
     */
    public Element convertTableSet(String tableSet, List<TableName> entries) {
        tableSet = safeName(tableSet);
        final Element root = new Element("tableSet");
        if (tableSet.length() > 0)
            root.setAttribute("name", tableSet);
        // Write the entries
        for (TableName tn : entries) {
            // All tables should belong to the same database.
            // That is not checked here, we just use the first table's db name.
            if (root.getAttribute("db") == null) {
                root.setAttribute("db", tn.getDatabase());
            }
            final Element el = new Element("item");
            el.setAttribute("schema", tn.getSchema());
            el.setAttribute("table", tn.getTable());
            root.addContent(el);
        }
        return root;
    }

    /**
     * Write the table set to an XML file
     * @param tableSet Table set name
     * @param entries Table set entries
     * @throws Exception
     */
    public void writeTableSet(String tableSet, List<TableName> entries)
            throws Exception {
        tableSet = safeName(tableSet);
        if (tableSet.length() == 0)
            throw new IllegalArgumentException("empty table set name");
        File f = new File(workDir, tableSet + ".xml");
        Document doc = new Document(convertTableSet(tableSet, entries));
        XMLOutputter xmlOutput = new XMLOutputter();
        xmlOutput.setFormat(Format.getPrettyFormat());
        try (FileWriter fw = new FileWriter(f)) {
            xmlOutput.output(doc, fw);
        }
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
