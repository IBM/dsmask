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

import java.util.List;

/**
 * Interface to the input link
 * @author zinal
 */
public interface LinkInput {

    /**
     * Retrieve the column information
     * @return List of column names and indexes
     */
    List<XColumnInfo> getInputColumns();

    /**
     * Provide a single row of input
     * @return Input row with values
     */
    RowInput readRecord();

}
