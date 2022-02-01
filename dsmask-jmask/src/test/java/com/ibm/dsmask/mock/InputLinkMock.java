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

import com.ibm.is.cc.javastage.api.*;
import java.util.List;
import java.util.Properties;

/**
 *
 * @author zinal
 */
public class InputLinkMock implements InputLink {

    private final MockData mockData;
    private int position = 0;

    public InputLinkMock(MockData cmb) {
        this.mockData = cmb;
    }

    public MockData getBuilder() {
        return mockData;
    }

    public int findIndex(String name) {
        int index = 1;
        for (ColumnMetadata cm : mockData.getColumnMetadata()) {
            if (name.equalsIgnoreCase(cm.getName()))
                return index;
            ++index;
        }
        return -1;
    }

    @Override
    public InputRecord readRecord() {
        if (position < mockData.getData().size()) {
            return new InputRecordMock(mockData, position++);
        }
        return null;
    }

    @Override
    public OutputLink getAssociatedRejectLink() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int getPartitionType() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public LinkType getLinkType() {
        return LinkType.INPUT;
    }

    @Override
    public int getLinkIndex() {
        return 1;
    }

    @Override
    public int getColumnCount() {
        return mockData.getColumnMetadata().size();
    }

    @Override
    public List<ColumnMetadata> getColumnMetadata() {
        return mockData.getColumnMetadata();
    }

    @Override
    public ColumnMetadata getColumn(int i) {
        return mockData.getColumnMetadata().get(i-1);
    }

    @Override
    public ColumnMetadata getColumn(String string) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Properties getUserProperties() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<ColumnMetadata> subtractColumnList(Link link) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

//    @Override
    public String getConnectorName() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
