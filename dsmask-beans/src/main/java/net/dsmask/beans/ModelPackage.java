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
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import net.dsmask.model.*;

/**
 * Per-package cache of loaded named model elements.
 * Contains zero or more model elements of various types.
 * Package may be fully or partially loaded.
 * @author zinal
 */
public class ModelPackage implements PackageAccessor {

    private final String name;
    private final Map<EntityType, Map<String,ModelEntity>> elements = new HashMap<>();

    public ModelPackage(String name) {
        this.name = ModelUtils.safe(name);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getId() {
        return ModelUtils.lower(name);
    }

    @Override
    public ModelEntity find(EntityType et, String name) {
        Map<String,ModelEntity> m = elements.get(et);
        if (m==null)
            return null;
        return m.get(ModelUtils.lower(name));
    }

    @Override
    public ModelEntity find(ModelName mn) {
        if (mn==null)
            return null;
        return find(mn.getEntityType(), mn.getName());
    }

    @Override
    public <ET extends ModelEntity> ET find(Class<ET> type, String name) {
        EntityType et = EntityType.CLAZZ.get(type);
        if (et == null)
            return null;
        @SuppressWarnings("unchecked")
        ET retval = (ET) find(et, name);
        return retval;
    }

    @Override
    public Collection<ModelName> list() {
        final ArrayList<ModelName> ret = new ArrayList<>();
        for ( Map<String,ModelEntity> m : elements.values() )
            ret.addAll(m.values());
        return ret;
    }

    @Override
    public Collection<ModelName> list(EntityType et) {
        Map<String,ModelEntity> m = elements.get(et);
        if (m==null)
            return Collections.emptyList();
        return Collections.unmodifiableCollection(m.values());
    }

    public ModelPackage addEntry(ModelEntity me) {
        Map<String,ModelEntity> m = elements.get(me.getEntityType());
        if (m==null) {
            m = new HashMap<>();
            elements.put(me.getEntityType(), m);
        }
        m.put(me.getId(), me);
        return this;
    }

    public <ET extends ModelEntity> ET put(ET me) {
        addEntry(me);
        return me;
    }

    public final MaskingLabel label(String name) {
        return find(MaskingLabel.class, name);
    }

    public final AlgorithmInfo algorithm(String name) {
        return AlgorithmProvider.getInstance().findAlgorithm(name);
    }

    public final MaskingFunction function(String name) {
        return find(MaskingFunction.class, name);
    }

    public final MaskingRule rule(String name) {
        return find(MaskingRule.class, name);
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 73 * hash + ModelUtils.hashCode(this.name);
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
        final ModelPackage other = (ModelPackage) obj;
        if (!ModelUtils.equalsCI(this.name, other.name)) {
            return false;
        }
        if (!Objects.equals(this.elements, other.elements)) {
            return false;
        }
        return true;
    }

}
