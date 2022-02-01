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

import org.jdom2.Element;
import net.dsmask.model.common.EntityType;

/**
 *
 * @author zinal
 */
public abstract class XmlProviderBase implements XmlObjectProvider {

    protected abstract Element loadEntity(String name, EntityType type);

    public class XmlPromise extends XmlObject {

        private final String name;

        public XmlPromise(String name, EntityType type) {
            super(type);
            this.name = name;
        }

        @Override
        public Element getElement() {
            if (element==null) {
                element = loadEntity(name, EntityType.Rule);
            }
            return element;
        }

    }

}
