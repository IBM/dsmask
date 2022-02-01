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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import net.dsmask.model.any.Utils;
import net.dsmask.model.any.RulesAccessor;

/**
 * Accessor implementation to a collection of metadata packages.
 * @author zinal
 */
public class ModelAccessor implements PackageAccessor, RulesAccessor {

    private final List<PackageAccessor> data;

    public ModelAccessor() {
        this.data = new ArrayList<>();
    }

    public ModelAccessor addPackage(PackageAccessor pa) {
        data.add(pa);
        return this;
    }

    public List<PackageAccessor> getPackages() {
        return data;
    }

    @Override
    public ModelEntity find(EntityType type, String name) {
        for (PackageAccessor pa : data) {
            ModelEntity me = pa.find(type, name);
            if (me!=null)
                return me;
        }
        return null;
    }

    @Override
    public ModelEntity find(ModelName mn) {
        for (PackageAccessor pa : data) {
            ModelEntity me = pa.find(mn);
            if (me!=null)
                return me;
        }
        return null;
    }

    @Override
    public <ET extends ModelEntity> ET find(Class<ET> type, String name) {
        for (PackageAccessor pa : data) {
            ET me = pa.find(type, name);
            if (me!=null)
                return me;
        }
        return null;
    }

    @Override
    public Collection<ModelName> list() {
        if (data.isEmpty())
            return Collections.emptyList();
        if (data.size() == 1)
            return data.iterator().next().list();
        final List<ModelName> retval = new ArrayList<>();
        for (PackageAccessor pa : data)
            retval.addAll(pa.list());
        return retval;
    }

    @Override
    public Collection<ModelName> list(EntityType type) {
        if (data.isEmpty())
            return Collections.emptyList();
        if (data.size() == 1)
            return data.iterator().next().list(type);
        final List<ModelName> retval = new ArrayList<>();
        for (PackageAccessor pa : data)
            retval.addAll(pa.list(type));
        return retval;
    }

    @Override
    public String getName() {
        final StringBuilder sb = new StringBuilder();
        sb.append("group[");
        for ( int i=0; i<data.size(); ++i ) {
            if (i>0)
                sb.append(",");
            sb.append(data.get(i).getName());
        }
        sb.append("]");
        return sb.toString();
    }

    @Override
    public String getId() {
        final StringBuilder sb = new StringBuilder();
        sb.append("group[");
        for ( int i=0; i<data.size(); ++i ) {
            if (i>0)
                sb.append(",");
            sb.append(data.get(i).getId());
        }
        sb.append("]");
        return sb.toString();
    }

    public MaskingRule findRule(String name) {
        return find(MaskingRule.class, name);
    }

    @Override
    public List<MaskingRule> retrieveRules(String context) {
        final List<MaskingRule> retval = new ArrayList<>();
        for (PackageAccessor pa : data) {
            for (ModelName name : pa.list(EntityType.Rule)) {
                MaskingRule rule = pa.find(MaskingRule.class, name.getName());
                if (rule==null)
                    continue;
                if (context==null || rule.getContexts().contains(context))
                    retval.add(rule);
            }
        }
        return retval;
    }

    public MetaTable findTable(String fullName) {
        for (PackageAccessor pa : data) {
            for (ModelName name : pa.list(EntityType.Metadata)) {
                MetaEntity mm = pa.find(MetaEntity.class, name.getName());
                if (mm==null)
                    continue;
                MetaTable mt = mm.getTable();
                if (mt.isIgnoreCase()) {
                    if (mt.getFullName().equalsIgnoreCase(fullName))
                        return mt;
                } else {
                    if (mt.getFullName().equals(fullName))
                        return mt;
                }
            }
        }
        return null;
    }

    public MetaTable findTable(String db, String schema, String table) {
        for (PackageAccessor pa : data) {
            for (ModelName name : pa.list(EntityType.Metadata)) {
                MetaEntity mm = pa.find(MetaEntity.class, name.getName());
                if (mm==null)
                    continue;
                MetaTable mt = mm.getTable();
                if (mt.isIgnoreCase()) {
                    if (!Utils.equalsCI(mt.getTableName(), table))
                        continue;
                    if (!Utils.equalsCI(mt.getSchemaName(), schema))
                        continue;
                    if (!Utils.equalsCI(mt.getDatabaseName(), db))
                        continue;
                } else {
                    if (!Utils.safeEquals(mt.getTableName(), table))
                        continue;
                    if (!Utils.safeEquals(mt.getSchemaName(), schema))
                        continue;
                    if (!Utils.safeEquals(mt.getDatabaseName(), db))
                        continue;
                }
                return mt;
            }
        }
        return null;
    }

}
