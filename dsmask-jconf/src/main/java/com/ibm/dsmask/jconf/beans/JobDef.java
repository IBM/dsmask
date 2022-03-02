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
package com.ibm.dsmask.jconf.beans;

/**
 * Masking job definition bean.
 * Contains just the properties which are not configurable
 * on the JobManager level.
 * @author zinal
 */
public class JobDef {

    public final String inputTable;
    public final String outputTable;
    public final String maskingProfile;

    public JobDef(String inputTable, String outputTable, String maskingProfile) {
        this.inputTable = inputTable;
        this.outputTable = outputTable;
        this.maskingProfile = maskingProfile;
    }

}
