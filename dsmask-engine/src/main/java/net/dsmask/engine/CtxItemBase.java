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
package net.dsmask.engine;

/**
 * Basic definitions for the computational step.
 * @author zinal
 */
public abstract class CtxItemBase {

    private XVector value;

    public CtxItemBase() {
        this.value = new XVector(1);
    }

    public final XVector getValue() {
        return value;
    }

}
