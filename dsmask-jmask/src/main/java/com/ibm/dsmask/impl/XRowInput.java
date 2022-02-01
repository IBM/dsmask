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
package com.ibm.dsmask.impl;

/**
 * The abstract access to the input row values
 * @author zinal
 */
public interface XRowInput {

    /**
     * Retrieve the field value by its index.
     * Indexes start from 1
     * @param index Field index
     * @return Field value (may be null)
     */
    Object getValue(int index);

}
