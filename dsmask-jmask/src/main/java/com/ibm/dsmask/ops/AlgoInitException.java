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
 * Custom exception thrown for fatal errors preventing masking
 * operation from proper functioning (mostly for lazy init errors).
 * @author zinal
 */
public class AlgoInitException extends RuntimeException {

    public AlgoInitException(String message) {
        super(message);
    }

    public AlgoInitException(String message, Throwable cause) {
        super(message, cause);
    }

}
