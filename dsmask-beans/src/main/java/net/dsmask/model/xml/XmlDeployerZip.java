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
import org.apache.commons.lang3.StringUtils;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.jdom2.Document;
import net.dsmask.model.*;

/**
 * Writes XML DOM data for the model entities into the ZIP archive.
 * @author zinal
 */
public class XmlDeployerZip implements XmlDeployer, AutoCloseable {

    private final File fileName;
    private final ZipOutputStream zos;

    public XmlDeployerZip(File fileName) throws IOException {
        this.fileName = fileName;
        this.fileName.delete();
        this.zos = new ZipOutputStream(new FileOutputStream(fileName));
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
