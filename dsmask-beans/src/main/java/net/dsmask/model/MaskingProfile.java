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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Table masking profile - a set of masking actions to be executed
 * over a specific table (chosen according to its fields data classes).
 * Combines the references to table information, masking rules and
 * field access definitions.
 * 
 * @author zinal
 * @since 2020
 */
public class MaskingProfile extends EntityBase {

    private final AnyTable table;
    private final List<MaskingOperation> operations = new ArrayList<>();

    public MaskingProfile(AnyTable table) {
        this(table, generateName(table));
    }

    public MaskingProfile(AnyTable table, String name) {
        super(name);
        this.table = table;
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.Profile;
    }

    public AnyTable getTable() {
        return table;
    }

    public List<MaskingOperation> getOperations() {
        return operations;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 41 * hash + Objects.hashCode(this.table);
        return hash;
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
        final MaskingProfile other = (MaskingProfile) obj;
        if (!Objects.equals(this.table, other.table)) {
            return false;
        }
        if (!Objects.equals(this.operations, other.operations)) {
            return false;
        }
        return true;
    }

    public static String generateName(AnyTable table) {
        return table.getFullName() + ".GN-" + UUID.randomUUID();
    }

}
