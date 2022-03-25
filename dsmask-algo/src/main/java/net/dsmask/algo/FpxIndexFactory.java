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
 * Index generator factory interface.
 * @author zinal
 */
public interface FpxIndexFactory {

    /**
     * Create the new index generator based on the input value and iteration value.
     * @param value Input value, must not be null
     * @param iteration Iteration value, must be null on initial iteration
     * @return New index generator for the current input and iteration values
     */
    FpxIndexGen make(String value, String iteration);

}
