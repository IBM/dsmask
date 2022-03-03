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

import java.util.Objects;
import net.dsmask.model.*;

/**
 * Supporting class to avoid the need to implement standard methods
 * of model entity classes.
 * Currently all model entity classes extend this one,
 * but this should not be relied on.
 * @author zinal
 */
public abstract class EntityBase implements ModelEntity {

    private final EntityType entityType;
    private final String name;

    public EntityBase(EntityType type, String name) {
        this.entityType = type;
        this.name = ModelUtils.safe(name);
    }

    @Override
    public EntityType getEntityType() {
        return entityType;
    }

    @Override
    public String getId() {
        return ModelUtils.lower(name);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final EntityBase other = (EntityBase) obj;
        if (this.entityType != other.entityType) {
            return false;
        }
        if (!Objects.equals(this.getId(), other.getId())) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 17 * hash + Objects.hashCode(this.entityType);
        hash = 17 * hash + Objects.hashCode(this.getId());
        return hash;
    }

    @Override
    public String toString() {
        return entityType + "(" + name + ')';
    }

}
