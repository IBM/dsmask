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
public class CtxStep extends CtxBase {

    private final ItemStep item;

    public CtxStep(ItemStep item) {
        this.item = item;
    }

    public final ItemStep getItem() {
        return item;
    }

}
