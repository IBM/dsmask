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
package com.ibm.dsmask.jconf.beans;

/**
 * Data class lookup interface.
 * @author zinal
 */
public interface DataClassLookup {

    /**
     * Find data class by name
     * @param name Name of a data class
     * @return Data class object, or null if one not found
     */
    DataClass find(String name);

    /**
     * Grab the full set of data classes as array.
     * @return Array of all data classes
     */
    DataClass[] collect();

    /**
     * @return Total number of known data classes
     */
    int size();

}
