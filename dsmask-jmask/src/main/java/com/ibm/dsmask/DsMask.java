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
package com.ibm.dsmask;

import java.util.ArrayList;
import java.util.List;
import java.io.File;
import java.util.Arrays;
import java.util.Properties;

import com.ibm.is.cc.javastage.api.*;
import com.ibm.dsmask.beans.*;
import com.ibm.dsmask.ds.DsLink;
import com.ibm.dsmask.impl.*;
import com.ibm.dsmask.uniq.UniqClientFactory;

/**
 * DataStage masking operator
 * @author zinal
 */
public class DsMask extends Processor {

    public static final String PROP_PATH_CONF = "ConfigPath";
    public static final String PROP_PATH_DICT = "DictPath";
    public static final String PROP_CONFIG = "ConfigName";
    public static final String PROP_PROF = "ProfileName";
    public static final String PROP_BATCH = "BatchSize";
    public static final String PROP_SCOPE_LIST = "ScopeList";
    public static final String PROP_UNIQ_HOST = "UniqHost";
    public static final String PROP_UNIQ_PORT = "UniqPort";
    public static final String PROP_UNIQ_SECRET = "UniqSecret";

    private InputLink inputLink;
    private OutputLink outputLink;
    private MskContext maskingContext;
    private XServices services;
    private XMasker masker;

    private int batchSize = 100;
    private String dictPath = null;
    private String configPath = null;
    private String configName = null;
    private String databaseName = null;
    private String tableName = null;
    private String profileName = null;
    private List<String> scopeList = null;
    private String uniqHost = null;
    private int uniqPort = -1;
    private String uniqSecret = null;

    private final List<String> configErrors = new ArrayList<>();

    /**
     * Configure the services instance externally.
     * This method is normally used only in tests, and should be invoked before
     * all other methods, to avoid auto-configuration of the services instance.
     * @param services The services instance to be used.
     */
    public void setServices(XServices services) {
        this.services = services;
    }

    @Override
    public List<PropertyDefinition> getUserPropertyDefinitions() {
        List<PropertyDefinition> propList = new ArrayList<>();
        propList.add(new PropertyDefinition(PROP_PATH_CONF, "",
                "Configuration path",
                "Specifies the path to the configuration directory.",
                PropertyDefinition.Scope.STAGE));
        propList.add(new PropertyDefinition(PROP_PATH_DICT, "",
                "Dictionary path",
                "Specifies the path to the dictionary directory.",
                PropertyDefinition.Scope.STAGE));
        propList.add(new PropertyDefinition(PROP_CONFIG, GlobalNames.DEF_CONFIG,
                "Configuration name",
                "Specifies the name of the configuration storage.",
                PropertyDefinition.Scope.STAGE));
        propList.add(new PropertyDefinition(PROP_PROF, "",
                "Profile name",
                "Specifies the profile in form DB.SCHEMA.TABLE.",
                PropertyDefinition.Scope.STAGE));
        propList.add(new PropertyDefinition(PROP_BATCH, "100",
                "Batch size",
                "Number of rows to be processed in a batch.",
                PropertyDefinition.Scope.STAGE));
        propList.add(new PropertyDefinition(PROP_SCOPE_LIST, "",
                "List of column name prefixes as a scope for masking",
                "Specifies a comma-separated list of column name prefixes, "
                        + "typical value is 'BEFORE_,AFTER_'",
                PropertyDefinition.Scope.STAGE));
        propList.add(new PropertyDefinition(PROP_UNIQ_HOST, "",
                "UNIQ-CHECK service hostname",
                "Specifies the hostname where UNIQ-CHECK service runs.",
                PropertyDefinition.Scope.STAGE));
        propList.add(new PropertyDefinition(PROP_UNIQ_PORT, "27500",
                "UNIQ-CHECK service port",
                "Specifies the port number where UNIQ-CHECK service runs.",
                PropertyDefinition.Scope.STAGE));
        propList.add(new PropertyDefinition(PROP_UNIQ_SECRET, "",
                "UNIQ-CHECK service secret",
                "Specifies the access secret for UNIQ-CHECK service.",
                PropertyDefinition.Scope.STAGE));
        return propList;
    }

    @Override
    public Capabilities getCapabilities() {
      Capabilities capabilities = new Capabilities();
      // we need the input data to mask
      capabilities.setMinimumInputLinkCount(1);
      capabilities.setMaximumInputLinkCount(1);
      // we need to write the masked data
      capabilities.setMinimumOutputStreamLinkCount(1);
      capabilities.setMaximumOutputStreamLinkCount(1);
      // sometimes masking failes, so we can use the reject link
      capabilities.setMinimumRejectLinkCount(0);
      capabilities.setMaximumRejectLinkCount(1);
      // no wave control
      capabilities.setIsWaveGenerator(false);
//      capabilities.setColumnTransferBehavior(ColumnTransferBehavior.COPY_COLUMNS_FROM_SINGLE_INPUT_TO_ALL_OUTPUTS);
      return capabilities;
    }

    @Override
    public boolean validateConfiguration(Configuration config, boolean runtime)
            throws Exception {

        configErrors.clear();

        parseProperties(config.getUserProperties());

        if (runtime) {
            inputLink = config.getInputLink(0);
            outputLink = config.getOutputLink(0);
            OutputLink rejectLink = null;
            if (config.getRejectLinkCount()>0) {
                rejectLink = inputLink.getAssociatedRejectLink();
            }
            DsLink dsLink = new DsLink(inputLink, outputLink, rejectLink);
            masker = new XMasker(dsLink, dsLink, batchSize);
            if (! masker.buildIndexMap()) {
                for (String cname : masker.getMissingColumns())
                    configErrors.add(cname + ": missing input column");
            }
            if (services==null)
                services = configureServices();
            masker.setServices(services);
        }

        return configErrors.isEmpty();
    }

    private void parseProperties(Properties props) {
        try {
            String tmp = props.getProperty(PROP_BATCH);
            if (tmp==null || tmp.length()==0) {
                batchSize = 100;
            } else {
                batchSize = Integer.valueOf(tmp);
            }
        } catch(Exception ex) {
            addConfError(PROP_BATCH, ex);
        }
        if (batchSize < 1 || batchSize > 100000)
            batchSize = 100;
        configPath = props.getProperty(PROP_PATH_CONF);
        if (configPath==null || configPath.length()==0) {
            configPath = System.getenv(GlobalNames.ENV_CONFIG);
        }
        if (configPath==null) {
            addConfError(PROP_PATH_CONF, "Not specified");
        } else if (new File(configPath).isDirectory()==false) {
            addConfError(PROP_PATH_CONF, "Not a directory");
        }
        dictPath = props.getProperty(PROP_PATH_DICT);
        if (dictPath==null || dictPath.length()==0) {
            dictPath = System.getenv(GlobalNames.ENV_DICT);
        }
        if (dictPath==null) {
            addConfError(PROP_PATH_DICT, "Not specified");
        } else if (new File(configPath).isDirectory()==false) {
            addConfError(PROP_PATH_DICT, "Not a directory");
        }
        configName = props.getProperty(PROP_CONFIG);
        configName = Utils.lower(configName);
        if (configName.length()==0)
            configName = GlobalNames.DEF_CONFIG;
        profileName = props.getProperty(PROP_PROF);
        if (profileName==null) {
            addConfError(PROP_PROF, "Not specified");
        } else {
            String[] parts = profileName.split("[.]");
            if (parts.length!=3 && parts.length!=2) {
                addConfError(PROP_PROF, "Invalid format");
            } else {
                databaseName = Utils.lower(parts[0]);
                if (parts.length==2) {
                    tableName = Utils.lower(parts[1]);
                } else {
                    tableName = Utils.lower(parts[1])
                            + "." + Utils.lower(parts[2]);
                }
                if (databaseName==null || databaseName.length()==0) {
                    addConfError(PROP_PROF, "Invalid format");
                }
                if (tableName==null || tableName.length()==0) {
                    addConfError(PROP_PROF, "Invalid format");
                }
            }
        }
        String scopeListVal = props.getProperty(PROP_SCOPE_LIST);
        if (scopeListVal!=null && scopeListVal.trim().length()!=0) {
            scopeList = new ArrayList<>();
            scopeList.addAll(Arrays.asList(scopeListVal.split("[,]")));
        }
        String uniqHostVal = props.getProperty(PROP_UNIQ_HOST);
        if (uniqHostVal!=null && uniqHostVal.trim().length()>0) {
            uniqHost = uniqHostVal;
            try {
                String tmp = props.getProperty(PROP_UNIQ_PORT);
                if (tmp==null || tmp.length()==0) {
                    uniqPort = 27500;
                } else {
                    uniqPort = Integer.valueOf(tmp);
                }
            } catch(Exception ex) {
                addConfError(PROP_UNIQ_PORT, ex);
            }
            uniqSecret = props.getProperty(PROP_UNIQ_SECRET);
        }
    }

    @Override
    public List<String> getConfigurationErrors() {
        final List<String> retval = new ArrayList<>();
        retval.addAll(configErrors);
        return retval;
    }

    private void addConfError(String propName, String text) {
        configErrors.add(propName + ": " + text);
    }

    private void addConfError(String propName, Throwable ex) {
        final StringBuilder sb = new StringBuilder();
        sb.append(propName).append(": ");
        sb.append(ex.getClass().getName());
        sb.append(" - ");
        sb.append(ex.getMessage());
        ex = ex.getCause();
        while (ex!=null) {
            sb.append("\n\t");
            sb.append(ex.getClass().getName());
            sb.append(" - ");
            sb.append(ex.getMessage());
            ex = ex.getCause();
        }
        configErrors.add(sb.toString());
    }

    @Override
    public void initialize() throws Exception {
        try (ContextLoader ctxLoader
                = new ContextLoader(configPath, configName)) {
            maskingContext = ctxLoader.load(databaseName, tableName, scopeList);
        } catch(Exception ex) {
            throw new Exception("Failed to load configuration database ["
                    + configName + "]", ex);
        }
        if (maskingContext!=null) {
            SafeLogger.information("Masking context loaded for "
                    + databaseName + "." + tableName);
            maskingContext.setDictPath(dictPath);
            masker.setContext(maskingContext);
            masker.validateFields();
            logMaskingOperations();
        } else {
            SafeLogger.information("No masking context for "
                    + databaseName + "." + tableName);
        }
    }

    @Override
    public void process() throws Exception {
        if (maskingContext==null) {
            // Nothing to mask.
            // Just copy records from input to output.
            // The same can be done through Masker.run(), but here
            // we try to be DataStage-friendly.
            while (true) {
                InputRecord ir = inputLink.readRecord();
                if (ir==null)
                    break; // no more input
                // Magic: copying of input happends behind the scenes
                //   in the writeRecord() method.
                // The same approach DOES NOT work for batch mode.
                OutputRecord or = outputLink.getOutputRecord();
                outputLink.writeRecord(or);
            }
        } else {
            // We have the masking configuration.
            // Run the masker.
            masker.run();
            if (masker.getPerfStats() != null) {
                SafeLogger.information("*** Performance statistics: \r\n"
                    + masker.getPerfStats().toString());
            }
        }
    }

    private void logMaskingOperations() {
        final List<ColumnMetadata> inputMeta = inputLink.getColumnMetadata();
        final List<ColumnMetadata> outputMeta = outputLink.getColumnMetadata();
        final StringBuilder ixInfo = new StringBuilder();
        ixInfo.append("** Field indexes for masking operations:");
        for (MskOp op : maskingContext.getTable().getOperations()) {
            int pos;
            boolean comma;
            ixInfo.append("\n*** rule ").append(op.getRule().getName());
            ixInfo.append("\n\tIN: ");
            comma = false;
            for (pos=0; pos<op.getInputIndexes().length; ++pos) {
                if (comma)
                    ixInfo.append(", ");
                comma = true;
                int index = op.getInputIndexes()[pos];
                ColumnMetadata cmdata = findColumn(inputMeta, index);
                if (cmdata==null)
                    ixInfo.append("<UNKNOWN>");
                else
                    ixInfo.append(cmdata.getName().toLowerCase());
                ixInfo.append(" @").append(index);
            }
            ixInfo.append("\n\tOUT: ");
            comma = false;
            for (pos=0; pos<op.getOutputIndexes().length; ++pos) {
                if (comma)
                    ixInfo.append(", ");
                comma = true;
                int index = op.getOutputIndexes()[pos];
                ColumnMetadata cmdata = findColumn(outputMeta, index);
                if (cmdata==null)
                    ixInfo.append("<UNKNOWN>");
                else
                    ixInfo.append(cmdata.getName().toLowerCase());
                ixInfo.append(" @").append(index);
            }
        }
        SafeLogger.information(ixInfo.toString());
    }

    private static ColumnMetadata findColumn(List<ColumnMetadata> cmlist,
            int index) {
        for (int i=0; i<cmlist.size(); ++i) {
            ColumnMetadata cur = cmlist.get(i);
            if (index == cur.getIndex())
                return cur;
        }
        return null;
    }

    /**
     * Build and setup the services instance.
     * @return The services instance to be used.
     */
    private XServices configureServices() {
        XServices xs = new XServices();
        if (uniqHost != null) {
            xs.setUniqProviderFactory(new UniqClientFactory(uniqHost, uniqPort, uniqSecret));
        }
        return xs;
    }

}
