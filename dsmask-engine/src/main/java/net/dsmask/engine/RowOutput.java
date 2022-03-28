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
 * The abstract interface to put values to the output rows.
 * @author zinal
 */
public interface RowOutput {

    /**
     * Set the field value by its index in a row.
     * Indexes start with 1
     * @param index Field index
     * @param value Field value, may be null
     */
    void setValue(int index, Object value);

    /**
     * Set the field value in its string representation,
     * performing a necessary type conversion.
     * @param index Field index
     * @param value Field value in a string form, may be null
     */
    void setValueAsString(int index, String value);

}
