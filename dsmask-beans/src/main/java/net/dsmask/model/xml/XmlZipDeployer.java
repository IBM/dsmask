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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apache.commons.lang3.StringUtils;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.jdom2.Document;
import net.dsmask.model.common.ModelName;

/**
 *
 * @author zinal
 */
public class XmlZipDeployer implements XmlObjectDeployer {

    private final File fileName;
    private final ZipOutputStream zos;

    public XmlZipDeployer(File fileName, String packageName) throws IOException {
        this.fileName = fileName;
        fileName.delete();
        this.zos = new ZipOutputStream(new FileOutputStream(fileName));
        Properties props = new Properties();
        props.setProperty(XmlNames.PROP_PACKAGE_NAME, packageName);
        zos.putNextEntry(new ZipEntry(XmlNames.PROP_PACKAGE_INFO));
        props.store(zos, getClass().getName());
        zos.closeEntry();
    }

    @Override
    public void save(ModelName mn, Element el) {
        try {
            Element root = new Element(XmlNames.TAG_Root);
            root.addContent(el);
            zos.putNextEntry(new ZipEntry(makeFileName(mn)));
            new XMLOutputter(Format.getPrettyFormat())
                    .output(new Document(root), zos);
            zos.closeEntry();
        } catch(Exception ex) {
            throw new RuntimeException("Failed to write file " + fileName, ex);
        }
    }

    private String makeFileName(ModelName mn) {
        return StringUtils.replaceChars(mn.getEntityType().name() + "-" + mn.getName() + ".xml",
                "/\\:", "---");
    }

    @Override
    public void close() throws IOException {
        zos.close();
    }

}
