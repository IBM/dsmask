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

import java.util.List;

/**
 * Interface to the input link
 * @author zinal
 */
public interface XLinkInput {

    /**
     * Retrieve the column information
     * @return List of column names and indexes
     */
    List<XColumnInfo> getInputColumns();

    /**
     * Provide a single row of input
     * @return Input row with values
     */
    XRowInput readRecord();

}
