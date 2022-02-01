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

/**
 * Interface for algorithms over a single input value.
 * @author zinal
 */
public interface AlSimpleValue {

    /**
     * Perform algorithm over the input value
     * @param in Input value
     * @return Output value
     */
    Object exec(Object in);

    /**
     * @return true, if iterations are supported, false otherwise
     */
    boolean isIterationsSupported();

    /**
     * Perform algorithm iteration over the input value
     * @param in Input value
     * @param iteration iteration number (0 - initial, positive number - subsequent)
     * @return Output value
     */
    Object exec(Object in, int iteration);

}
