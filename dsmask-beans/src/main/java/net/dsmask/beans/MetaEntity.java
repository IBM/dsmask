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

/**
 * Masking metadata entity consists of a single table definition.
 * @author zinal
 */
public class MetaEntity extends EntityBase {

    private final MetaTable table;

    public MetaEntity(MetaTable table) {
        super(EntityType.Metadata, table.isIgnoreCase() ? 
                table.getFullName().toLowerCase() : table.getFullName());
        this.table = table;
    }

    public MetaTable getTable() {
        return table;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (! super.equals(obj)) {
            return false;
        }
        final MetaEntity other = (MetaEntity) obj;
        if (!Objects.equals(this.table, other.table)) {
            return false;
        }
        return true;
    }

}
