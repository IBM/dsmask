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
 * Batch executor of a particular algorithm.
 * @author zinal
 */
public interface XExecutor {

    /**
     * Run masking algorithm over inputs, store results into the outputs.
     * @param space Computation workspace
     */
    void exec(XWorkspace space);

}
