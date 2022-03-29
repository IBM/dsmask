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

import net.dsmask.engine.*;
import net.dsmask.model.*;

/**
 * Basic definitions for the computational step.
 * @author zinal
 */
public abstract class CtxBase {

    protected final ItemType itemType;
    protected XVector value;
    
    public CtxBase(ItemType itemType) {
        this(itemType, 1);
    }

    public CtxBase(ItemType itemType, int size) {
        this.itemType = itemType;
        this.value = new XVector(size);
    }

    public final ItemType getItemType() {
        return itemType;
    }

    public final XVector getValue() {
        return value;
    }

    public final XVector makeValue(int size) {
        value = XVector.make(value, size);
        return value;
    }

}
