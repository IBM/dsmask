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
 * Package metadata accessor interface.
 * @author zinal
 */
public interface ModelAccessor extends ModelPackager {

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
     * Retrieve the masking rules for the specified context and labels.
     * @param context Masking context, null or empty string for default
     * @param labels Collection of field labels
     * @return Collection of masking rules
     */
    Collection<? extends AnyRule> retrieveRules(String context,
            Collection<? extends AnyLabel> labels);

}
