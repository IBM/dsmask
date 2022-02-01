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
package net.dsmask.model.common;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import net.dsmask.model.any.AnyReference;

/**
 * Reference to a set of data class labels
 * to be used as arguments or outputs of a masking rule.
 * @author zinal
 */
public class MetaReference implements AnyReference {

    private final Set<MaskingLabel> labels = new HashSet<>();

    public MetaReference() {
    }

    public MetaReference(MaskingLabel ... mls) {
        for (MaskingLabel ml : mls) {
            if (ml==null)
                throw new NullPointerException();
            labels.add(ml);
        }
    }

    @Override
    public Set<MaskingLabel> getLabels() {
        return Collections.unmodifiableSet(labels);
    }

    public MetaReference addLabel(MaskingLabel ml) {
        if (ml!=null)
            labels.add(ml);
        return this;
    }

    public MetaReference addLabels(Collection<MaskingLabel> mls) {
        if (mls!=null) {
            for (MaskingLabel ml : mls) {
                if (ml!=null)
                    labels.add(ml);
            }
        }
        return this;
    }

    public MetaReference clearLabels() {
        labels.clear();
        return this;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 67 * hash + Objects.hashCode(this.labels);
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
        final MetaReference other = (MetaReference) obj;
        if (!Objects.equals(this.labels, other.labels)) {
            return false;
        }
        return true;
    }

}
