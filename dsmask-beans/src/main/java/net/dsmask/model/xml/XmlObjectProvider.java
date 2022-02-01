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

import java.util.Collection;
import net.dsmask.model.common.EntityType;

/**
 * Generic interface to access XML data containing the model entities.
 * It is expected that normally all related entities are located in a single package
 * (package export/import facilities are not designed yet).
 *
 * @author zinal
 */
public interface XmlObjectProvider {

    /**
     * @return The name of the package loaded (unnormalized)
     */
    String getPackageName();

    /**
     * Retrieve the XML data for the named entity of the specified type.
     * Entity name will be normalized during the search.
     * Different entity types can contain same names.
     * @param et Entity type
     * @param name Entity name
     * @return XML data, or null if the entity was not found.
     */
    XmlObject getObject(EntityType et, String name);

    /**
     * Read all XML data in the package
     * @return Unordered collection of all XML data in the package.
     */
    Collection<XmlObject> enumObjects();

    /**
     * Read all XML data for the entities of the specified type.
     * @param et Entity type
     * @return Unordered collection of all XML data of this type in the package.
     */
    Collection<XmlObject> enumObjects(EntityType et);

}
