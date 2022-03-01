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
package com.ibm.dsmask.jconf.impl;

/**
 * Interface to get configuration settings for the JobManager.
 * @author zinal
 */
public interface JobConfiguration {

    /**
     * Retrieve the option value for the specified option.
     * Throws exception if the option value has not been set.
     * @param name Option name
     * @return Option value
     */
    String getOption(String name);

}
