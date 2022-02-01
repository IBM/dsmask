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
package com.ibm.dsmask.uniq;

import java.io.Serializable;

/**
 * The response from the uniq-check service.
 * Can contain one of the following:
 * (a) confirmation that the specified masked value is linked
 *     to the specified source value;
 * (b) indication that there is a conflict, e.g. the specified
 *     masked value is already linked to a different source value;
 * (c) indication that there is a conflict, plus the information
 *     about the conflict resolution (e.g. the iteration number).
 * @author zinal
 */
public class UniqResponse implements Serializable {

    static final long serialVersionUID = 2020071501L;

    private boolean linkedCorrectly = false;
    private int iteration = -1;

    public UniqResponse() {
        this.linkedCorrectly = false;
        this.iteration = -1;
    }

    public UniqResponse(boolean linkedCorrectly) {
        this.linkedCorrectly = linkedCorrectly;
        this.iteration = -1;
    }

    public UniqResponse(boolean linkedCorrectly, int iteration) {
        this.linkedCorrectly = linkedCorrectly;
        this.iteration = iteration;
    }

    public boolean isLinkedCorrectly() {
        return linkedCorrectly;
    }

    public void setLinkedCorrectly(boolean linkedCorrectly) {
        this.linkedCorrectly = linkedCorrectly;
    }

    public int getIteration() {
        return iteration;
    }

    public void setIteration(int iteration) {
        this.iteration = iteration;
    }

}
