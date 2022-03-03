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
package net.dsmask.beans.xml;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jdom2.Element;
import org.jdom2.located.Located;
import org.apache.commons.lang3.StringUtils;
import net.dsmask.beans.EntityType;
import net.dsmask.model.ModelUtils;

/**
 *
 * @author zinal
 */
public abstract class XmlBasicProvider implements XmlObjectProvider {

    protected final Map<EntityType, Map<String, XmlObject>> map = new HashMap<>();
    protected final List<XmlObject> entries = new ArrayList<>();

    public XmlBasicProvider() {
    }

    protected final void handleItem(String fname, Element element) {
        EntityType et = EntityType.TAGS.get(element.getName());
        if (et==null) {
            raise(fname, element, "unsupported XML tag [" + element.getName() + "]");
            return; // unreached
        }
        String entityName = element.getAttributeValue("name");
        if (StringUtils.isBlank(entityName)) {
            raise(fname, element, "missing entity name");
            return; // unreached
        }
        Map<String, XmlObject> m = map.get(et);
        if (m==null) {
            m = new HashMap<>();
            map.put(et, m);
        }
        String key = ModelUtils.lower(entityName);
        if (m.containsKey(key)) {
            raise(fname, element, "duplicate name for " + et.name() + ": " + entityName);
            return; // unreached
        }
        element.setAttribute(XmlObject.FILE_NAME, fname);
        XmlObject xo = new XmlObject(element, et);
        m.put(key, xo);
        entries.add(xo);
    }

    protected static void raise(String fname, Element el, CharSequence message) {
        if (el instanceof Located) {
            Located l = (Located) el;
            throw new RuntimeException("Parse error at line "
                    + String.valueOf(l.getLine()) + ", column "
                    + String.valueOf(l.getColumn()) + " of file [" + fname + "]: " + message);
        } else {
            throw new RuntimeException("Parse error at unknown location of file ["
                    + fname + "]: " + message);
        }
    }

    @Override
    public XmlObject getObject(EntityType et, String name) {
        if (StringUtils.isBlank(name))
            return null;
        final Map<String, XmlObject> m = map.get(et);
        if (m==null)
            return null;
        return m.get(ModelUtils.lower(name));
    }

    @Override
    public Collection<XmlObject> enumObjects() {
        if (map.isEmpty())
            return Collections.emptyList();
        return Collections.unmodifiableList(entries);
    }

    @Override
    public Collection<XmlObject> enumObjects(EntityType et) {
        final Map<String, XmlObject> m = map.get(et);
        if (m==null)
            return Collections.emptySet();
        return Collections.unmodifiableCollection(m.values());
    }

}
