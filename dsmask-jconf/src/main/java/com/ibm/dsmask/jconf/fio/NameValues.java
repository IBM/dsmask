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
package com.ibm.dsmask.jconf.fio;

/**
 * Pseudo-bean for a 3-part name
 * @author mzinal
 */
public class NameValues {

    public final String first;
    public final String last;
    public final String middle;
    public final String full;

    public NameValues(String first, String last, String middle) {
        this.first = first;
        this.last = last;
        this.middle = middle;
        this.full = last + " " + first +
                ((middle==null || middle.length()==0) ? ""
                : " " + middle);
    }

}
