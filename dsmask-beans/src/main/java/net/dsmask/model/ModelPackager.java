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
package net.dsmask.model;

import java.util.Collection;

/**
 * The collection of methods to query model entities by their types and names.
 * @author zinal
 */
public interface ModelPackager {

    /**
     * @param mn Entity type and identity
     * @return true, if the entity exists, and false otherwise
     */
    boolean exists(ModelName mn);

    /**
     * @param type Entity type
     * @param name Entity name
     * @return true, if the entity exists, and false otherwise
     */
    boolean exists(EntityType type, String name);

    /**
     * List all model entities.
     * @return Collection of model entity names, in no specific order
     */
    Collection<ModelName> list();

    /**
     * List all model entities of the specified type.
     * @param type Entity type
     * @return Collection of model entity names, in no specific order
     */
    Collection<ModelName> list(EntityType type);

}
