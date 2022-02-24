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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
        final List<Element> dbs = root.getChildren("db");
        if (dbs==null || dbs.isEmpty())
            return Collections.emptyList();
        final List<TableName> retval = new ArrayList<>();
        for (Element db : dbs) {
            final List<Element> tables = db.getChildren("table");
            if (tables==null || tables.isEmpty())
                continue;
            final String dbname = db.getAttributeValue("name");
            for (Element tab : tables) {
                final TableName tn = new TableName(
                        dbname, tab.getAttributeValue("name") );
                if (tn.isValid())
                    retval.add(tn);
            }
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
        // Group the entries by the database name
        final Map<String, List<TableName>> dbs = new HashMap<>();
        for (TableName tn : entries) {
            if (!tn.isValid())
                continue;
            List<TableName> l = dbs.get(tn.getDatabase());
            if (l==null) {
                l = new ArrayList<>();
                dbs.put(tn.getDatabase(), l);
            }
            l.add(tn);
        }
        // Write the entries
        for (Map.Entry<String, List<TableName>> me : dbs.entrySet()) {
            Element elDb = new Element("db");
            elDb.setAttribute("name", me.getKey());
            for (TableName tn : me.getValue()) {
                final Element el = new Element("table");
                el.setAttribute("db", tn.getDatabase());
                el.setAttribute("name", tn.getName());
                elDb.addContent(el);
            }
            root.addContent(elDb);
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
