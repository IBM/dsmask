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

import java.io.FileWriter;
import java.util.List;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import com.ibm.dsmask.jconf.beans.*;

/**
 *
 * @author zinal
 */
public class TableInfoXmlWriter {

    public Element buildTree(List<TableInfo> values) {
        Element root = new Element("dsmask-table-info");
        for (TableInfo ti : values) {
            Element table = new Element("table");
            table.setAttribute("db", ti.getDatabase());
            table.setAttribute("name", ti.getName());
            for (FieldInfo fi : ti.getFields()) {
                Element field = new Element("field");
                field.setAttribute("name", fi.getName());
                field.setAttribute("dcs", fi.getDcsString());
                table.addContent(field);
            }
            root.addContent(table);
        }
        return root;
    }

    public void write(String fname, List<TableInfo> values) throws Exception {
        Element data = buildTree(values);
        Document doc = new Document(data);
        XMLOutputter xmlOutput = new XMLOutputter();
        xmlOutput.setFormat(Format.getPrettyFormat());
        try (FileWriter fw = new FileWriter(fname)) {
            xmlOutput.output(doc, fw);
        }
    }

}
