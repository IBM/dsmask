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

import com.ibm.is.cc.javastage.api.InputRecord;
import java.lang.reflect.InvocationTargetException;

/**
 *
 * @author zinal
 */
public class InputRecordMock implements InputRecord {

    private final MockData mockData;
    private final int position;

    public InputRecordMock(MockData mockData, int position) {
        this.mockData = mockData;
        this.position = position;
    }

    @Override
    public Object getObject() throws IllegalAccessException, InstantiationException, InvocationTargetException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Object getValue(String name) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Object getValue(int i) {
        return mockData.getData().get(position)[i-1];
    }

//    @Override
    public int getSize(){
        return 1024;
    }

}
