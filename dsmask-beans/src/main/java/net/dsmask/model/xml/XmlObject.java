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
package net.dsmask.model.xml;

import java.util.List;
import org.jdom2.Element;
import org.jdom2.located.Located;
import org.apache.commons.lang3.StringUtils;
import net.dsmask.model.common.*;
import net.dsmask.model.any.Utils;

/**
 * Entity parsing support infrastructure.
 * Utility methods on top of Jdom element tree representing a single model entity.
 * @author zinal
 */
public class XmlObject implements ModelName {

    public static final String FILE_NAME = "private-file-name";

    protected final EntityType type;
    protected Element element;

    public XmlObject(Element element, EntityType type) {
        this.element = element;
        this.type = type;
    }

    protected XmlObject(EntityType type) {
        this.element = null;
        this.type = type;
    }

    public Element getElement() {
        return element;
    }

    @Override
    public EntityType getEntityType() {
        return type;
    }

    public String getNameAndType() {
        return type.name() + ":" + getElement().getAttributeValue("name");
    }

    public static String getFileName(Element el) {
        String fname = null;
        Element cur = el;
        while (fname == null && cur != null) {
            fname = cur.getAttributeValue(FILE_NAME);
            cur = cur.getParentElement();
        }
        if (fname != null)
            return fname;
        cur = el;
        while (cur != null) {
            fname = cur.getAttributeValue("name");
            cur = cur.getParentElement();
        }
        if (fname != null)
            return fname;
        return "<unknown>.xml";
    }

    public static String getPosition(Element el) {
        if (el instanceof Located) {
            Located l = (Located) el;
            return "line " + String.valueOf(l.getLine())
                    + ", column " + String.valueOf(l.getColumn());
        } else {
            return "position unknown";
        }
    }

    public RuntimeException raise(CharSequence message) {
        return raise(getElement(), message);
    }

    public RuntimeException raise(CharSequence message, Throwable cause) {
        return raise(getElement(), message, cause);
    }

    public static RuntimeException raise(Element el, CharSequence message) {
        return new RuntimeException("Parse error at "
                + getPosition(el) + " in file [" + getFileName(el) + "]: "
                + message
        );
    }

    public static RuntimeException raise(Element el, CharSequence message, Throwable cause) {
        return new RuntimeException("Parse error at "
                + getPosition(el) + " in file [" + getFileName(el) + "]: "
                + message, cause
        );
    }

    public static RuntimeException raiseMissing(Element el, String name) {
        return raise(el, "Missing attribute '" + name + "' for tag '" + el.getName() + "'");
    }

    public static RuntimeException raiseIllegal(Element el, String name) {
        return raise(el, "Illegal value for attribute '" + name + "' in tag '"
                + el.getName() + "': [" + el.getAttributeValue(name) + "]");
    }

    @Override
    public String getName() {
        return getName(getElement());
    }

    @Override
    public String getId() {
        return Utils.lower(getName());
    }

    public List<Element> getChildren() {
        return element.getChildren();
    }

    public List<Element> getChildren(String name) {
        return element.getChildren(name);
    }

    public static String getName(Element el) {
        String v = el.getAttributeValue("name");
        if (StringUtils.isBlank(v)) {
            throw raiseMissing(el, "name");
        }
        v = v.trim();
        if (! Character.isLetter(v.charAt(0))) {
            throw raiseIllegal(el, "name");
        }
        return v;
    }

    public String getAttr(String name) {
        return getAttr(getElement(), name);
    }

    public static String getAttr(Element el, String name) {
        String v = el.getAttributeValue(name);
        if (v==null) {
            throw raiseMissing(el, name);
        }
        return v;
    }

    public String getAttr(String name, String defval) {
        return getElement().getAttributeValue(name, defval);
    }

    public static String getAttr(Element el, String name, String defval) {
        return el.getAttributeValue(name, defval);
    }

    public static String getText(Element el) {
        return el.getTextTrim();
    }

    public boolean getBool(String name) {
        return getBool(getElement(), name);
    }

    public boolean getBool(String name, boolean defval) {
        return getBool(getElement(), name, defval);
    }

    public static Boolean parseBoolean(String vs) {
        if (vs==null)
            return null;
        vs = vs.trim().toLowerCase();
        if (vs.length()==0)
            return null;
        switch (vs.charAt(0)) {
            case '1': case 't': case 'y':
                return Boolean.TRUE;
            case '0': case 'f': case 'n':
                return Boolean.FALSE;
        }
        return null;
    }

    public static boolean getBool(Element el, String name) {
        String vs = el.getAttributeValue(name);
        if (StringUtils.isBlank(vs))
            throw raiseMissing(el, name);
        Boolean vb = parseBoolean(vs);
        if (vb == null)
            throw raiseIllegal(el, name);
        return vb;
    }

    public static boolean getBool(Element el, String name, boolean defval) {
        String vs = el.getAttributeValue(name);
        if (StringUtils.isBlank(vs))
            return defval;
        Boolean vb = parseBoolean(vs);
        if (vb == null)
            throw raiseIllegal(el, name);
        return vb;
    }

    public int getInt(String name) {
        return getInt(getElement(), name);
    }

    public int getInt(String name, int defval) {
        return getInt(getElement(), name, defval);
    }

    public static int getInt(Element el, String name) {
        String vs = el.getAttributeValue(name);
        if (StringUtils.isBlank(vs))
            throw raiseMissing(el, name);
        try {
            return Integer.parseInt(vs.trim());
        } catch(NumberFormatException nfe) {
            throw raiseIllegal(el, name);
        }
    }

    public static int getInt(Element el, String name, int defval) {
        String vs = el.getAttributeValue(name);
        if (StringUtils.isBlank(vs))
            return defval;
        try {
            return Integer.parseInt(vs.trim());
        } catch(NumberFormatException nfe) {
            throw raiseIllegal(el, name);
        }
    }

    public long getLong(String name) {
        return getLong(getElement(), name);
    }

    public long getLong(String name, int defval) {
        return getLong(getElement(), name, defval);
    }

    public static long getLong(Element el, String name) {
        String vs = el.getAttributeValue(name);
        if (StringUtils.isBlank(vs))
            throw raiseMissing(el, name);
        try {
            return Long.parseLong(vs.trim());
        } catch(NumberFormatException nfe) {
            throw raiseIllegal(el, name);
        }
    }

    public static long getLong(Element el, String name, int defval) {
        String vs = el.getAttributeValue(name);
        if (StringUtils.isBlank(vs))
            return defval;
        try {
            return Long.parseLong(vs.trim());
        } catch(NumberFormatException nfe) {
            throw raiseIllegal(el, name);
        }
    }

}
