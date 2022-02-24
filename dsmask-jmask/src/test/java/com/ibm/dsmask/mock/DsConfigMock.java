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

import com.ibm.dsmask.DsMask;
import com.ibm.is.cc.javastage.api.*;
import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

/**
 *
 * @author zinal
 */
public class DsConfigMock implements Configuration, AutoCloseable {

    private final MockData mockData;
    private final InputLinkMock inputLink;
    private final OutputLinkMock outputLink;

    public DsConfigMock(String tableName) {
        this.mockData = new MockData(tableName);
        this.inputLink = new InputLinkMock(mockData);
        this.outputLink = new OutputLinkMock(mockData);
    }

    @Override
    public void close() {
        outputLink.close();
    }

    @Override
    public Properties getRuntimeEnvironment() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<Link> getLinks() {
        return Arrays.asList(inputLink, outputLink);
    }

    @Override
    public List<InputLink> getInputLinks() {
        return Collections.singletonList(inputLink);
    }

    @Override
    public List<OutputLink> getOutputLinks() {
        return Collections.singletonList(outputLink);
    }

    @Override
    public int getInputLinkCount() {
        return 1;
    }

    @Override
    public int getOutputLinkCount() {
        return 1;
    }

    @Override
    public int getStreamOutputLinkCount() {
        return 0;
    }

    @Override
    public int getRejectLinkCount() {
        return 0;
    }

    @Override
    public InputLink getInputLink(int i) {
        return inputLink;
    }

    @Override
    public OutputLink getOutputLink(int i) {
        return outputLink;
    }

    @Override
    public OutputLink getStreamOutputLink(int i) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Properties getUserProperties() {
        final Properties props = new Properties();
        File path = new File("..");
        path = new File(path, "testsuite");
        path = new File(path, "rules");
        props.setProperty(DsMask.PROP_PATH_CONF, path.getAbsolutePath());
        path = new File(new File(new File(".."), "dict-data"), "ru");
        props.setProperty(DsMask.PROP_PATH_DICT, path.getAbsolutePath());
        props.setProperty(DsMask.PROP_CONFIG, "dsmask-testsuite");
        props.setProperty(DsMask.PROP_PROF, "source." + mockData.getTableName());
        props.setProperty(DsMask.PROP_BATCH, "11");
        return props;
    }

    @Override
    public int getNodeNumber() {
        return 1;
    }

    @Override
    public int getNodeCount() {
        return 1;
    }

    @Override
    public SparseLookupMode getSparseLookupMode() {
        return SparseLookupMode.NOT_SPARSE;
    }

    @Override
    public DataChannel getDataChannel() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

//    @Override
    public String getConnectorName() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
