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
package net.dsmask.engine.impl;

import net.dsmask.model.*;

/**
 *
 * @author zinal
 */
public class CtxScript extends CtxBase {

    private final ItemScript item;

    public CtxScript(ItemScript item) {
        super(ItemType.Script, item.getInputs().size());
        this.item = item;
    }

    public final ItemScript getItem() {
        return item;
    }

    public final void calculate() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
