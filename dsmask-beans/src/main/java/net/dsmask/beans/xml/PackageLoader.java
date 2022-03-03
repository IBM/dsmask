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
package net.dsmask.beans.xml;

import java.io.File;
import net.dsmask.beans.ModelPackage;

/**
 * Loads the full model package from a set of input XML documents.
 * @author zinal
 */
public class PackageLoader extends ModelLoader {

    public PackageLoader(XmlObjectProvider provider) {
        super(provider);
    }

    public PackageLoader(String path, String packageName) {
        this(new File(path), packageName);
    }

    public PackageLoader(File path, String packageName) {
        super( path.isDirectory() ?
                new XmlDirectoryProvider(path, packageName) :
                new XmlZipProvider(path) );
    }

    /**
     * Load the complete model package.
     * @return Package with the full set of entities.
     */
    public ModelPackage loadPackage() {
        initParser();
        for (XmlObject xo : provider.enumObjects()) {
            if (pool.find(xo.getEntityType(), xo.getName()) == null)
                handleEntity(xo);
        }
        return pool;
    }

}
