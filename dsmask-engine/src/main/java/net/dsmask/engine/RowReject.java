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
 * Row rejection explanation.
 * @author zinal
 */
public interface RowReject {

    /**
     * Set the row rejection reason code
     * @param code Reason code
     */
    void setErrorCode(int code);

    /**
     * Set the row rejection reason text
     * @param rejectText Reason text
     */
    void setErrorText(String rejectText);

}
