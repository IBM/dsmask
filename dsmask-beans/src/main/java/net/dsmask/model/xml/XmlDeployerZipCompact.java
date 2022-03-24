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
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.jdom2.Document;
import net.dsmask.model.*;

/**
 * The version of a ZIP deployer to allow more compact model
 * representation, both to save space and for faster reading.
 * @author zinal
 */
public class XmlDeployerZipCompact implements XmlDeployer, AutoCloseable {

    private final File fileName;
    private final ZipOutputStream zos;
    private final Element rootElement;

    public XmlDeployerZipCompact(File fileName) throws IOException {
        this.fileName = fileName;
        fileName.delete();
        this.zos = new ZipOutputStream(new FileOutputStream(fileName));
        rootElement = new Element(XmlNames.TAG_Root);
    }

    @Override
    public void save(ModelName mn, Element el) {
        rootElement.addContent(el);
    }

    @Override
    public void close() throws IOException {
        try {
            zos.putNextEntry(new ZipEntry(XmlNames.PROP_PACKAGE_DATA));
            new XMLOutputter(Format.getRawFormat())
                    .output(new Document(rootElement), zos);
            zos.closeEntry();
            zos.close();
        } catch(Exception ex) {
            throw new RuntimeException("Failed to write file " + fileName, ex);
        }
    }

}
