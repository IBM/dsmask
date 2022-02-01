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
package com.ibm.dsmask.mock;

/**
 *
 * @author zinal
 */
public class MaskingMockColumn {

    private final int number;
    private final String name;
    private final MockDCS[] dcs;

    public MaskingMockColumn(int number, String name, MockDCS[] dcs) {
        this.number = number;
        this.name = name;
        this.dcs = (dcs==null) ? new MockDCS[0] : dcs;
    }

    public int getNumber() {
        return number;
    }

    public String getName() {
        return name;
    }

    public MockDCS[] getDcs() {
        return dcs;
    }

}
