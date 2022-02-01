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
package com.ibm.dsmask.ops;

import com.ibm.dsmask.impl.XVector;

/**
 * Interface of algorithms running over a vector of values, taken from
 * a single row if input table.
 * @author zinal
 */
public interface AlSimpleVector {

    /**
     * Execute the algorithm.
     * @param in Vector of input values
     * @param out Previous vector, to avoid unnecessary re-allocation
     * @return Vector of resulting values
     */
    XVector exec(XVector in, XVector out);

    /**
     * @return true, if iterations are supported, false otherwise
     */
    boolean isIterationsSupported();

    /**
     * Execute the algorithm iteration.
     * @param in Vector of input values
     * @param out Previous vector, to avoid unnecessary re-allocation
     * @param iteration iteration number (0 - initial, positive number - subsequent)
     * @return Vector of resulting values
     */
    XVector exec(XVector in, XVector out, int iteration);

}
