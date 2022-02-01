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

import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.ArrayList;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import com.ibm.dsmask.jconf.beans.*;

/**
 *
 * @author zinal
 */
public class TableInfoXmlReader {

    public List<TableInfo> readList(org.jdom2.Element start) throws Exception {
        final ArrayList<TableInfo> retval = new ArrayList<>();
        for (org.jdom2.Element cur : start.getChildren("table")) {
            final TableInfo ti = readTable(cur);
            if (ti!=null)
                retval.add(ti);
        }
        return retval;
    }

    public List<TableInfo> readList(InputStream is) throws Exception {
        return readList(new SAXBuilder().build(is).getRootElement());
    }

    public List<TableInfo> readList(File f) throws Exception {
        return readList(new SAXBuilder().build(f).getRootElement());
    }

    public List<TableInfo> readList(String pathname) throws Exception {
        return readList(new SAXBuilder().build(pathname).getRootElement());
    }

    public TableInfo readTable(Element start) throws Exception {
        final TableInfo ti = new TableInfo (
                start.getAttributeValue("db"),
                start.getAttributeValue("name")
        );
        for (org.jdom2.Element cur : start.getChildren("field")) {
            final FieldInfo fi = readField(cur);
            if (fi!=null)
                ti.addField(fi);
        }
        return ti.isValid() ? ti : null;
    }

    private FieldInfo readField(Element start) throws Exception {
        final FieldInfo fi = new FieldInfo(start.getAttributeValue("name"));
        final String dcs = start.getAttributeValue("dcs");
        if (dcs!=null) {
            for (String dc : dcs.split("[;, ]")) {
                fi.addDc(dc);
            }
        }
        return fi.isValid() ? fi : null;
    }

}
