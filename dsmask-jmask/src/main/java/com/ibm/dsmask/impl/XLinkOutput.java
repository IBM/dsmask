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

import java.util.List;

/**
 * Interface to the output link
 * @author zinal
 */
public interface XLinkOutput {

    /**
     * Retrieve the column information
     * @return List of column names and indexes
     */
    List<XColumnInfo> getOutputColumns();

    /**
     * Generate a new output record object
     * @return Empty output record
     */
    XRowOutput getOutputRecord();

    /**
     * Save an output record
     * @param record Output record object
     */
    void writeRecord(XRowOutput record);

    /**
     * @return true, if the job has a reject link defined
     */
    boolean hasRejectLink();

    /**
     * Generate a new reject link object for the specified input record
     * @param inputRecord Input record rejected
     * @return Empty reject record
     */
    XRowReject getRejectRecord(XRowInput inputRecord);

    /**
     * Save a reject record
     * @param rejectRecord  Reject record object
     */
    void writeRecord(XRowReject rejectRecord);

}
