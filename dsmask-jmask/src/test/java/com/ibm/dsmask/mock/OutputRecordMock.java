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
import com.ibm.is.cc.javastage.api.OutputLink;
import com.ibm.is.cc.javastage.api.OutputRecord;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author zinal
 */
public class OutputRecordMock implements OutputRecord {

    private final OutputLink parent;
    private final Map<Integer, Object> values = new TreeMap<>();

    public OutputRecordMock(OutputLink parent) {
        this.parent = parent;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("(");
        boolean needcomma = false;
        for (Object v : values.values()) {
            if (needcomma)
                sb.append(", ");
            if (v==null)
                sb.append("<null>");
            else
                sb.append(v.toString());
            needcomma = true;
        }
        sb.append(")");
        return sb.toString();
    }

    @Override
    public void putObject(Object o) throws IllegalAccessException, InvocationTargetException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setValue(String name, Object o) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setValue(int i, Object o) {
        values.put(i, o);
    }

    @Override
    public void setValueAsString(String name, String value) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setValueAsString(int i, String value) {
        values.put(i, value);
    }

    @Override
    public void copyColumnsFromInputRecord(InputRecord ir) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public OutputLink getOfLink() {
        return parent;
    }

}
