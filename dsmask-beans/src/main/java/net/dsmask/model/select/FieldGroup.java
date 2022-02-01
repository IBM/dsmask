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
package net.dsmask.model.select;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import net.dsmask.model.any.*;

/**
 * Field group - unordered collection of fields.
 * Has a name/identifier, however, it is not used to compare groups.
 * @author zinal
 */
public class FieldGroup {

    private final String identity;
    private final Set<AnyField> fields = new HashSet<>();
    private transient Map<String, AnyField> dcMap = null;

    public FieldGroup(String identity) {
        this.identity = Utils.lower(identity);
    }

    public FieldGroup(AnyLabel label) {
        this.identity = label.getId();
    }

    public String getIdentity() {
        return identity;
    }

    public final Set<AnyField> getFields() {
        return fields;
    }

    public final boolean add(AnyField fi) {
        return fields.add(fi);
    }

    public final boolean addAll(Collection<AnyField> fis) {
        return fields.addAll(fis);
    }

    public final boolean isEmpty() {
        return fields.isEmpty();
    }

    /**
     * Check for intersection between this and other field groups.
     * @param other Other field group
     * @return true, if this group intersects with other, false otherwise
     */
    public final boolean intersects(FieldGroup other) {
        for (AnyField fi : fields) {
            if (other.getFields().contains(fi))
                return true;
        }
        return false;
    }

    /**
     * Prepare the mapping of data class to field info.
     * The mapping should not include ambiguous cases.
     * @return The map data class to field information
     */
    public final Map<String, AnyField> makeDcMap() {
        if (dcMap!=null)
            return dcMap;
        final Map<String, AnyField> retval = new HashMap<>();
        final Set<String> multiClazz = new HashSet<>();
        for (AnyField fi : fields) {
            for (AnyLabel label : fi.getLabels()) {
                final String dc = label.getId();
                if (multiClazz.contains(dc))
                    continue; // skip ambiguous classes
                if (retval.remove(dc)!=null) {
                    // new ambiguous class
                    multiClazz.add(dc);
                } else {
                    retval.put(dc, fi);
                }
            }
        }
        dcMap = retval;
        return dcMap;
    }

    public final void clearDcMap() {
        dcMap = null;
    }

    public AnyField findField(AnyReference arg) {
        final Map<String, AnyField> map = makeDcMap();
        for (AnyLabel label : arg.getLabels()) {
            AnyField fi = map.get(label.getId());
            if (fi!=null)
                return fi;
        }
        return null;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + Objects.hashCode(this.fields);
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
        final FieldGroup other = (FieldGroup) obj;
        if (!Objects.equals(this.fields, other.fields)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "FieldGroup{" + identity + ", " + fields + '}';
    }

    /**
     * Compare the groups by their sizes
     */
    static class SizeComparator implements Comparator<FieldGroup> {
        @Override
        public int compare(FieldGroup o1, FieldGroup o2) {
            if (o1==o2)
                return 0;
            if (o1==null)
                return 1;
            if (o2==null)
                return -1;
            return o2.getFields().size() - o1.getFields().size();
        }
    } // class SizeComparator

}
