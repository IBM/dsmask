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
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.located.LocatedJDOMFactory;

/**
 * Reads XML model entities from a directory tree.
 * @author zinal
 */
public class XmlProviderFiles extends XmlProviderBase {

    public XmlProviderFiles(String dir) {
        this(new File(dir));
    }

    public XmlProviderFiles(File dir) {
        if (dir.isFile()) {
            loadFile(dir);
        } else {
            // Find the names of files to be loaded
            final List<String> files = new ArrayList<>();
            try {
                Files.walk(Paths.get(dir.getAbsolutePath()))
                        .filter(Files::isRegularFile)
                        .forEach((f) -> {
                            String fname = f.toString();
                            if (fname.endsWith(".xml"))
                                files.add(fname);
                        });
            } catch(IOException ix) {
                throw new RuntimeException("Failed to read the input directory " + dir, ix);
            }
            if (files.isEmpty()) {
                throw new RuntimeException("No model files in directory " + dir);
            }
            // Parse the files, and collect the model elements
            for (String fname : files) {
                loadFile(new File(fname));
            }
        }
    }

    private void loadFile(File file) {
        final Element cur;
        try {
            // TODO: validate XML against schema
            cur = new SAXBuilder(null, null, new LocatedJDOMFactory())
                .build(file).detachRootElement();
        } catch(JDOMException jx) {
            throw new RuntimeException("Failed to parse the input file " + file, jx);
        } catch(IOException ix) {
            throw new RuntimeException("Failed to read the input file " + file, ix);
        }
        for ( Element item : new ArrayList<>(cur.getChildren()) ) {
            handleItem(file.getName(), item.detach());
        }
    }

}
