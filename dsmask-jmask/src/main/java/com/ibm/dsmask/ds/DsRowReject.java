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
package com.ibm.dsmask.ds;

import com.ibm.dsmask.impl.XRowReject;
import com.ibm.is.cc.javastage.api.RejectRecord;

/**
 * DataStage adapter to Masker interface - reject row implementation.
 * @author zinal
 */
public class DsRowReject implements XRowReject {

    private final RejectRecord reject;

    public RejectRecord getReject() {
        return reject;
    }

    public DsRowReject(RejectRecord reject) {
        this.reject = reject;
    }

    @Override
    public void setErrorCode(int code) {
        reject.setErrorCode(code);
    }

    @Override
    public void setErrorText(String rejectText) {
        reject.setErrorText(rejectText);
    }

}
