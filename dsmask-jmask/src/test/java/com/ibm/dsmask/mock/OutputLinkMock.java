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

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Date;
import java.util.List;
import java.util.Arrays;
import java.util.Properties;
import java.nio.charset.StandardCharsets;
import com.ibm.is.cc.javastage.api.*;

/**
 *
 * @author zinal
 */
public class OutputLinkMock implements OutputLink, AutoCloseable {

    private final MockData mockData;
    private final PrintWriter writer;

    public OutputLinkMock(MockData mockData) {
        this.mockData = mockData;
        try {
            File file = new File(System.getProperty("java.io.tmpdir"),
                    "dsmask-test-output.txt");
            System.out.println("Masking trace file: " + file);
            writer = new PrintWriter(new OutputStreamWriter(
                    new FileOutputStream(file, true), StandardCharsets.UTF_8));
            writer.println(new Date() . toString());
            writer.println("*** Table " + mockData.getTableName());
            writer.println("*** Input follows.");
            for ( Object[] val : mockData.getData() )
                writer.println(Arrays.toString(val));
            writer.println("*** Output follows.");
        } catch(Exception ex) {
            throw new RuntimeException("Source data dump failed", ex);
        }
    }

    @Override
    public void close() {
        try {
            writer.close();
        } catch(Exception ex) {}
    }

    @Override
    public OutputRecord getOutputRecord() {
        return new OutputRecordMock(this);
    }

    @Override
    public OutputRecord getOutputRecord(InputRecord ir) {
        final OutputRecordMock retval = new OutputRecordMock(this);
        for (int i=0; i<mockData.getColumnMetadata().size(); ++i)
            retval.setValue(i, ir.getValue(i));
        return retval;
    }

    @Override
    public RejectRecord getRejectRecord(InputRecord ir) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void writeRecord(OutputRecord or) {
        writer.println("OUTREC: " + or.toString());
    }

    @Override
    public void writeRecord(RejectRecord rr) {
        /*noop*/
    }

    @Override
    public void writeWaveMarker() {
        /*noop*/
    }

    @Override
    public boolean isRcpEnabled() {
        return true;
    }

    @Override
    public LinkType getLinkType() {
        return LinkType.OUTPUT;
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
