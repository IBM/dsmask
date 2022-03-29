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

import net.dsmask.engine.RowInput;

/**
 *
 * @author zinal
 */
public class CtxRoot extends CtxBase {

    public CtxRoot(int[] index) {
        super(index.length);
    }
    
    public final void setup(int[] index, RowInput row) {
        makeValue(index.length);
        for (int i=0; i<index.length; ++i)
            value.values[i] = row.getValue(index[i]);
    }

}
