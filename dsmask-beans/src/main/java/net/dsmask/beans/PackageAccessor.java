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
package net.dsmask.beans;

import java.util.Collection;

/**
 * Package metadata accessor interface.
 * @author zinal
 */
public interface PackageAccessor extends ModelIdentity {

    /**
     * Find the requested model entity.
     * @param type Entity type
     * @param name Entity name
     * @return Model entity object, or null if it does not exist.
     */
    ModelEntity find(EntityType type, String name);

    /**
     * Find the requested model entity.
     * @param mn Entity name and type.
     * @return Model entity object, or null if it does not exist.
     */
    ModelEntity find(ModelName mn);

    /**
     * Find the requested model entity.
     * @param <ET> Entity type
     * @param type Entity type in the form of a class
     * @param name Entity name
     * @return Model entity object, or null if it does not exist.
     */
    <ET extends ModelEntity> ET find(Class<ET> type, String name);

    /**
     * List all model entities.
     * @return Collection of model entity names, in no specific order
     */
    Collection<ModelName> list();

    /**
     * List all model entities of the specified type.
     * @param type
     * @return Collection of model entity names, in no specific order
     */
    Collection<ModelName> list(EntityType type);

}
