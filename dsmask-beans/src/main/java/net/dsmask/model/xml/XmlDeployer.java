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
import net.dsmask.model.ModelName;

/**
 * The interface to save model entities as XML-formatted data.
 * @author zinal
 */
public interface XmlDeployer {

    /**
     * Save model entity to the underlying repository.
     * @param mn Model entity type and identity
     * @param el Model entity data, formatted as JDOM tree.
     */
    void save(ModelName mn, Element el);

}
