/*
 * Copyright (c) IBM Corp. 2018, 2022.
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

import java.util.Collection;
import net.dsmask.model.AnyLabel;
import net.dsmask.model.AnyRule;

/**
 * In-memory model loader with non-indexed retrieveRules() implementation.
 * @author zinal
 */
public class ModelLoaderMemory extends ModelLoaderBase {

    public ModelLoaderMemory(XmlProvider provider) {
        super(provider);
    }

    @Override
    public Collection<? extends AnyRule> retrieveRules(String context,
            Collection<? extends AnyLabel> labels) {
        // TODO: implementation
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
