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
import org.apache.commons.lang3.StringUtils;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.jdom2.Document;
import net.dsmask.model.*;

/**
 * Writes XML DOM model entity data to the set of files.
 * @author zinal
 */
public class XmlDeployerFiles implements XmlDeployer {

    private final File baseDir;

    public XmlDeployerFiles(File baseDir) throws IOException {
        if (baseDir.exists()) {
            if (!baseDir.isDirectory())
                throw new IllegalArgumentException("Must be a directory: " + baseDir);
        } else {
            baseDir.mkdirs();
        }
        this.baseDir = baseDir;
    }

    @Override
    public void save(ModelName mn, Element el) {
        File f = new File(baseDir, makeFileName(mn));
        try {
            Element root = new Element(XmlNames.TAG_Root);
            root.addContent(el);
            try (FileOutputStream fos = new FileOutputStream(f)) {
                new XMLOutputter(Format.getPrettyFormat())
                        .output(new Document(root), fos);
            }
        } catch(Exception ex) {
            throw new RuntimeException("Failed to write file " + f);
        }
    }

    private String makeFileName(ModelName mn) {
        return StringUtils.replaceChars(mn.getEntityType().name() + "-" + mn.getName() + ".xml",
                "/\\:", "---");
    }

}
