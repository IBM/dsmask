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

import net.dsmask.model.*;

/**
 * Name and type of a model entity, to be returned from enumeration methods.
 * Implementation class.
 * @author zinal
 */
public class ModelNameX implements ModelName {

    private final EntityType entityType;
    private final String name;

    public ModelNameX(EntityType entityType, String name) {
        this.entityType = entityType;
        this.name = name;
    }

    @Override
    public EntityType getEntityType() {
        return entityType;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getId() {
        return ModelUtils.lower(name);
    }

}
