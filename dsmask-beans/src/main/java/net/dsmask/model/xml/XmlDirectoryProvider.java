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
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.located.LocatedJDOMFactory;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author zinal
 */
public class XmlDirectoryProvider extends XmlBasicProvider {

    private final String packageName;

    public XmlDirectoryProvider(String dir, String packageName) {
        this(new File(dir), packageName);
    }

    public XmlDirectoryProvider(String dir) {
        this(new File(dir), null);
    }

    public XmlDirectoryProvider(File dir) {
        this(dir, null);
    }

    public XmlDirectoryProvider(File dir, String packageName) {
        if (StringUtils.isBlank(packageName)) {
            this.packageName = readPackageName(dir);
        } else {
            this.packageName = packageName;
            dir = new File(dir, packageName);
        }
        // Find the names of files to be loaded
        final List<String> files = new ArrayList<>();
        try {
            Files.walk(Paths.get(dir.getAbsolutePath())).filter(Files::isRegularFile)
                    .forEach((f) -> {
                        String fname = f.toString();
                        if (fname.endsWith(".xml"))
                            files.add(fname);
                    });
        } catch(IOException ix) {
            throw new RuntimeException("Failed to read the input directory " + dir, ix);
        }
        if (files.isEmpty())
            throw new RuntimeException("No model files in directory " + dir);
        // Parse the files, and collect the model elements
        for (String fname : files) {
            final Element cur;
            try {
                // TODO: validate XML against schema
                cur = new SAXBuilder(null, null, new LocatedJDOMFactory())
                    .build(fname).detachRootElement();
            } catch(JDOMException jx) {
                throw new RuntimeException("Failed to parse the input file " + fname, jx);
            } catch(IOException ix) {
                throw new RuntimeException("Failed to read the input file " + fname, ix);
            }
            for ( Element item : new ArrayList<>(cur.getChildren()) ) {
                handleItem(fname, item.detach());
            }
        }
    }

    public static String readPackageName(File dir) {
        Properties props = new Properties();
        File f = new File(dir, XmlNames.PROP_PACKAGE_INFO);
        try (FileInputStream fis = new FileInputStream(f)) {
            props.load(fis);
        } catch(IOException ix) {
            throw new RuntimeException("Failed to read package properties from " + f, ix);
        }
        return props.getProperty(XmlNames.PROP_PACKAGE_NAME, "");
    }

    @Override
    public String getPackageName() {
        return packageName;
    }

}
