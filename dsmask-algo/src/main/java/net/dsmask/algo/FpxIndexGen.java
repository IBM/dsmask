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
package net.dsmask.algo;

/**
 * Index generator interface
 * @author zinal
 */
public interface FpxIndexGen {

    /**
     * Retrieve the next index in an array of specified size
     * @param charCount Size of an array
     * @return Index value from 0 to size-1
     */
    int nextIndex(int charCount);

}
