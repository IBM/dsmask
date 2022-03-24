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

/**
 * Name and type of a model entity, to be returned from enumeration methods.
 * It does not implement ModelEntity, because it is not a model entity itself,
 * just a reference to it by the type + name.
 * @author zinal
 */
public class ModelName implements ModelIdentity {

    private final EntityType entityType;
    private final String name;

    public ModelName(EntityType entityType, String name) {
        this.entityType = entityType;
        this.name = name;
    }

    /**
     * @return Model entity type
     */
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
