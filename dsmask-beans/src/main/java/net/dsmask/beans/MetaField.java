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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import net.dsmask.model.*;

/**
 * The description of a single field.
 * A field has a name and a set of associated labels for its data classes.
 * Data type topics are handled separately, no need for type information here.
 * @author zinal
 */
public class MetaField implements AnyField {

    private final String name;
    private final List<String> publicTags = new ArrayList<>();
    private final LinkedHashMap<String, MaskingLabel> labels = new LinkedHashMap<>();

    public MetaField(String name) {
        this.name = ModelUtils.safe(name);
    }

    public MetaField(String name, MaskingLabel[] labels) {
        this(name, null, labels);
    }

    public MetaField(String name, String[] tags) {
        this(name, tags, null);
    }

    public MetaField(String name, String[] tags, MaskingLabel[] labels) {
        this.name = ModelUtils.safe(name);
        if (tags != null) {
            for (String tag : tags)
                this.publicTags.add(ModelUtils.lower(tag));
        }
        if (labels != null) {
            for (MaskingLabel ml : labels)
                this.labels.put(ml.getId(), ml);
        }
    }

    @Override
    public String getName() {
        return name;
    }

    public List<String> getPublicTags() {
        return Collections.unmodifiableList(publicTags);
    }

    public void setPublicTags(List<String> tags) {
        publicTags.clear();
        if (tags != null) {
            tags.forEach(tag -> {
                publicTags.add(ModelUtils.lower(tag));
            });
        }
    }

    @Override
    public Collection<MaskingLabel> getLabels() {
        return Collections.unmodifiableCollection(labels.values());
    }

    public void setLabels(Collection<MaskingLabel> mls) {
        labels.clear();
        if (mls!=null) {
            mls.forEach(ml -> {
                labels.put(ml.getId(), ml);
            });
        }
    }

    /**
     * Check whether the field is confidential or not.
     * A field is considered to be confidential if it has a confidential label assigned.
     * @return true, if the field is confidential, false otherwise.
     */
    @Override
    public boolean isConfidential() {
        return ( getConfidLabel() != null );
    }

    /**
     * Find and return the label having a confidential status.
     * Normally there should be one or zero confidential labels.
     * @return Confidential label, or null if one was not found.
     */
    public MaskingLabel getConfidLabel() {
        for (MaskingLabel ml : labels.values()) {
            if (ml.isConfidential())
                return ml;
        }
        return null;
    }

    public void clearLabels() {
        labels.clear();
    }

    public MetaField addLabel(MaskingLabel ml) {
        labels.put(ml.getId(), ml);
        return this;
    }

    public MetaField addLabels(Collection<MaskingLabel> mls) {
        if (mls!=null) {
            for (MaskingLabel ml : mls) {
                labels.put(ml.getId(), ml);
            }
        }
        return this;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 19 * hash + Objects.hashCode(this.name);
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
        final MetaField other = (MetaField) obj;
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        if (!Objects.equals(this.publicTags, other.publicTags)) {
            return false;
        }
        if (!Objects.equals(this.labels, other.labels)) {
            return false;
        }
        return true;
    }

}
