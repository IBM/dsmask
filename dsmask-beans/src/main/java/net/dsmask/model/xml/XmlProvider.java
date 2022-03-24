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

import net.dsmask.model.*;

/**
 * Generic interface to access XML data containing the model entities.
 *
 * @author zinal
 */
public interface XmlProvider extends ModelPackager {

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
     * Retrieve the XML data for the named entity of the specified type.
     * Entity name will be normalized during the search.
     * Different entity types can contain same names.
     * @param mn Entity type and identity
     * @return XML data, or null if the entity was not found.
     */
    XmlObject getObject(ModelName mn);

}
