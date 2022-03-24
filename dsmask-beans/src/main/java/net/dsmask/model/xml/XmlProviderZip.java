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
package net.dsmask.model.xml;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.jdom2.located.LocatedJDOMFactory;

/**
 * Reads XML model entities from a ZIP archive.
 * @author zinal
 */
public class XmlProviderZip extends XmlProviderBase {

    public XmlProviderZip(String zipFile) {
        this(new File(zipFile));
    }

    public XmlProviderZip(File zipFile) {
        String pn = "";
        try ( FileInputStream fis = new FileInputStream(zipFile);
                BufferedInputStream bis = new BufferedInputStream(fis);
                ZipInputStream zis = new ZipInputStream(bis) ) {
            ZipEntry ze;
            while ((ze = zis.getNextEntry()) != null) {
                if (ze.isDirectory())
                    continue;
                String fname = ze.getName();
                if (fname.endsWith(".xml")) {
                    // TODO: validate XML against schema
                    final Element root = new SAXBuilder(null, null, new LocatedJDOMFactory())
                        . build(new InputStreamWrapper(zis)) . detachRootElement();
                    for (Element cur : root.getChildren())
                        handleItem(ze.getName(), cur);
                }
            }
        } catch(Exception ex) {
            throw new RuntimeException("Failed to load model file " + zipFile, ex);
        }
    }

}
