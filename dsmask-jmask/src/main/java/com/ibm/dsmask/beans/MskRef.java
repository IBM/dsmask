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
package com.ibm.dsmask.beans;

/**
 * Masking reference bean.
 * @author zinal
 */
public class MskRef {

    // the parent step - from which the source value is taken
    private MskStep parent;
    // parent step index, zero-based (-1=base input fields)
    private int parentIndex;
    // position of value in the parent state vector, 1-based
    private int position;

    public MskStep getParent() {
        return parent;
    }

    public void setParent(MskStep parent) {
        this.parent = parent;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public int getParentIndex() {
        return parentIndex;
    }

    public void setParentIndex(int parentIndex) {
        this.parentIndex = parentIndex;
    }

}
