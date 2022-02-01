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
import org.jdom2.input.SAXBuilder;
import org.jdom2.Element;
import com.ibm.dsmask.jconf.beans.*;

/**
 *
 * @author zinal
 */
public class DataClassRulesXmlReader {

    public void read(DataClassRules dcr, Element start) throws Exception {
        for (Element el : start.getChildren("dc-rule")) {
            final DataClassMode mode = DataClassMode.getMode
                (el.getAttributeValue("mode"));
            final boolean regexp = Boolean.valueOf
                (el.getAttributeValue("regexp"));
            String value = el.getAttributeValue("value");
            dcr.addEntry(mode, value, regexp);
        }
    }

    public void read(DataClassRules dcr, InputStream is) throws Exception {
        read(dcr, new SAXBuilder().build(is).getRootElement());
    }

    public void read(DataClassRules dcr, File f) throws Exception {
        read(dcr, new SAXBuilder().build(f).getRootElement());
    }

    public void read(DataClassRules dcr, String pathname) throws Exception {
        read(dcr, new File(pathname));
    }

    public DataClassRules readAndPrepare(Element start) throws Exception {
        final DataClassRules dcr = new DataClassRules();
        read(dcr, start);
        dcr.prepare();
        return dcr;
    }

    public DataClassRules readAndPrepare(InputStream is) throws Exception {
        return readAndPrepare(new SAXBuilder().build(is).getRootElement());
    }

    public DataClassRules readAndPrepare(File f) throws Exception {
        return readAndPrepare(new SAXBuilder().build(f).getRootElement());
    }

    public DataClassRules readAndPrepare(String pathname) throws Exception {
        return readAndPrepare(new File(pathname));
    }

}
