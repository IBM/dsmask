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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import net.dsmask.model.xml.XmlNames;

/**
 * Model entity types.
 * @author zinal
 */
public enum EntityType {

    /**
     * Execution pipeline fragment.
     */
    Fragment(XmlNames.TAG_Fragment, MaskingFragment.class),

    /**
     * Masking function.
     */
    Function(XmlNames.TAG_Function, MaskingFunction.class),

    /**
     * Initialization key.
     */
    Key(XmlNames.TAG_Key, MaskingKey.class),

    /**
     * Masking rule.
     */
    Rule(XmlNames.TAG_Rule, MaskingRule.class),

    /**
     * Field label (data class).
     */
    Label(XmlNames.TAG_Label, MaskingLabel.class),

    /**
     * Masking label selector based on the defined public tags.
     */
    Selector(XmlNames.TAG_Selector, LabelSelector.class),

    /**
     * Table metadata set.
     */
    Metadata(XmlNames.TAG_Metadata, MetaEntity.class),

    /**
     * Table masking profile.
     */
    Profile(XmlNames.TAG_Profile, MaskingProfile.class);

    public final String tag;
    public final Class<? extends ModelEntity> clazz;

    private EntityType(String tag, Class<? extends ModelEntity> clazz) {
        this.tag = tag;
        this.clazz = clazz;
    }

    public static final Map<String, EntityType> TAGS;
    public static final Map<Class<?>, EntityType> CLAZZ;
    static {
        Map<String, EntityType> mt = new HashMap<>();
        Map<Class<?>, EntityType> mc = new HashMap<>();
        for (EntityType et : EntityType.values()) {
            mt.put(et.tag, et);
            mc.put(et.clazz, et);
        }
        TAGS = Collections.unmodifiableMap(mt);
        CLAZZ = Collections.unmodifiableMap(mc);
    }

}
