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

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.ibm.is.cc.javastage.api.*;
import com.ibm.dsmask.apply.*;
import com.ibm.dsmask.impl.AbstractRunner;

/**
 * DataStage commitstream apply operator.
 * Given the records from DsMerge, sorted over the transaction number,
 * provides the ability to apply the changes to the target database.
 * @author mzinal
 */
public class DsApply extends Processor {

    public static final String PROP_PATH_CONF = "ConfigPath";
    public static final String PROP_JDBC_URL = "JdbcURL";
    public static final String PROP_JDBC_USER = "JdbcUser";
    public static final String PROP_JDBC_PASS = "JdbcPass";
    public static final String PROP_FIELD_TABLE = "FieldTable";
    public static final String PROP_FIELD_BEFORE = "FieldBefore";
    public static final String PROP_FIELD_AFTER = "FieldAfter";

    public static final int MAX_RETRIES = 5;

    private String configPath;
    private String jdbcURL;
    private String jdbcUser;
    private String jdbcPassword;
    private String fieldTableName;
    private String fieldBeforeName;
    private String fieldAfterName;

    private final List<String> configErrors = new ArrayList<>();

    private int fieldTableIndex = -1;
    private int fieldBeforeIndex = -1;
    private int fieldAfterIndex = -1;

    private InputLink inputLink;
    private Connection connection;
    private final Map<String, DstTableAction> actions = new HashMap<>();

    private final AbstractRunner logger = new AbstractRunner() {};

    @Override
    public List<PropertyDefinition> getUserPropertyDefinitions() {
        List<PropertyDefinition> propList = new ArrayList<>();
        propList.add(new PropertyDefinition(PROP_PATH_CONF, "",
                "Configuration path",
                "Specifies the path to the configuration directory.",
                PropertyDefinition.Scope.STAGE));
        propList.add(new PropertyDefinition(PROP_JDBC_URL, "",
                "Target DB JDBC URL",
                "JDBC URL for the destination database",
                PropertyDefinition.Scope.STAGE));
        propList.add(new PropertyDefinition(PROP_JDBC_USER, "",
                "Target DB JDBC username",
                "The name of a user for the destination database.",
                PropertyDefinition.Scope.STAGE));
        propList.add(new PropertyDefinition(PROP_JDBC_PASS, "",
                "Target DB JDBC password",
                "The password of a user for the destination database.",
                PropertyDefinition.Scope.STAGE));
        propList.add(new PropertyDefinition(PROP_FIELD_TABLE, "",
                "Table name field name",
                "Name of the input field with the table name.",
                PropertyDefinition.Scope.STAGE));
        propList.add(new PropertyDefinition(PROP_FIELD_BEFORE, "",
                "BEFORE pack field name",
                "Name of the input field with packed BEFORE values.",
                PropertyDefinition.Scope.STAGE));
        propList.add(new PropertyDefinition(PROP_FIELD_AFTER, "",
                "AFTER pack field name",
                "Name of the input field with packed AFTER values.",
                PropertyDefinition.Scope.STAGE));
        return propList;
    }

    @Override
    public Capabilities getCapabilities() {
      Capabilities capabilities = new Capabilities();
      // we need the input data to merge
      capabilities.setMinimumInputLinkCount(1);
      capabilities.setMaximumInputLinkCount(1);
      // we have no output
      capabilities.setMinimumOutputStreamLinkCount(0);
      capabilities.setMaximumOutputStreamLinkCount(0);
      // reject link is not supported
      capabilities.setMinimumRejectLinkCount(0);
      capabilities.setMaximumRejectLinkCount(0);
      // no wave control
      capabilities.setIsWaveGenerator(false);
//      capabilities.setColumnTransferBehavior(ColumnTransferBehavior.COPY_COLUMNS_FROM_SINGLE_INPUT_TO_ALL_OUTPUTS);
      return capabilities;
    }

    @Override
    public boolean validateConfiguration(Configuration config, boolean runtime)
            throws Exception {
        configErrors.clear();
        final Properties props = config.getUserProperties();

        configPath = props.getProperty(PROP_PATH_CONF);
        if (configPath==null || configPath.length()==0) {
            configPath = System.getenv(GlobalNames.ENV_CONFIG);
        }
        if (configPath==null) {
            addConfError(PROP_PATH_CONF, "Not specified");
        } else if (new File(configPath).isDirectory()==false) {
            addConfError(PROP_PATH_CONF, "Not a directory");
        }

        jdbcURL = props.getProperty(PROP_JDBC_URL);
        if (jdbcURL==null || jdbcURL.isEmpty()) {
            addConfError(PROP_JDBC_URL, "Not specified");
        }
        jdbcUser = props.getProperty(PROP_JDBC_USER);
        if (jdbcUser==null || jdbcUser.isEmpty()) {
            addConfError(PROP_JDBC_USER, "Not specified");
        }
        jdbcPassword = props.getProperty(PROP_JDBC_PASS);
        if (jdbcPassword==null) {
            addConfError(PROP_JDBC_PASS, "Not specified");
        }

        fieldTableName = props.getProperty(PROP_FIELD_TABLE);
        if (fieldTableName==null || fieldTableName.isEmpty()) {
            addConfError(PROP_FIELD_TABLE, "Not specified");
        }
        fieldBeforeName = props.getProperty(PROP_FIELD_BEFORE);
        if (fieldBeforeName==null || fieldBeforeName.isEmpty()) {
            addConfError(PROP_FIELD_BEFORE, "Not specified");
        }
        fieldAfterName = props.getProperty(PROP_FIELD_AFTER);
        if (fieldAfterName==null || fieldAfterName.isEmpty()) {
            addConfError(PROP_FIELD_AFTER, "Not specified");
        }

        if (runtime) {
            inputLink = config.getInputLink(0);
            // Ensure we have the input columns we need
            fieldTableIndex = -1;
            fieldBeforeIndex = -1;
            fieldAfterIndex = -1;
            for ( ColumnMetadata inCol : inputLink.getColumnMetadata() ) {
                if (inCol.getName().equalsIgnoreCase(fieldTableName))
                    fieldTableIndex = inCol.getIndex();
                else if (inCol.getName().equalsIgnoreCase(fieldBeforeName))
                    fieldBeforeIndex = inCol.getIndex();
                else if (inCol.getName().equalsIgnoreCase(fieldAfterName))
                    fieldAfterIndex = inCol.getIndex();
            }
            if (fieldTableIndex < 0)
                addConfError(PROP_FIELD_TABLE, "Cannot find field for table name");
            if (fieldBeforeIndex < 0)
                addConfError(PROP_FIELD_BEFORE, "Cannot find field for BEFORE pack");
            if (fieldAfterIndex < 0)
                addConfError(PROP_FIELD_AFTER, "Cannot find field for AFTER pack");
            // Load the settings to apply the tables
            final List<DstTableConf> tables = DstTableConf.loadAll(configPath);
            if (tables.isEmpty()) {
                 addConfError(PROP_PATH_CONF, "No table definitions found");
            } else {
                // Build the map of actions
                for (DstTableConf dtc : tables) {
                    if (dtc.isValid()) {
                        actions.put(dtc.getCommonName().toLowerCase(),
                                new DstTableAction(dtc));
                    } else {
                        configErrors.addAll(dtc.getErrors());
                    }
                }
            }
        }

        return configErrors.isEmpty();
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

    @Override
    public void initialize() throws Exception {
        connection = DriverManager.getConnection(jdbcURL, jdbcUser, jdbcPassword);
        connection.setAutoCommit(false);
        connection.setReadOnly(false);
    }

    @Override
    public void terminate(boolean isAborted) throws Exception {
        for (DstTableAction dta : actions.values())
            dta.close();
        actions.clear();
        if (connection != null) {
            try { connection.rollback(); } catch(Exception ex) {}
            try { connection.close(); } catch(Exception ex) {}
            connection = null;
        }
    }

    @Override
    public void process() throws Exception {
        final DstRowValue value = new DstRowValue();
        int commitCounter = 0;
        while (true) {
            InputRecord ir = inputLink.readRecord();
            if (ir==null)
                break; // no more input
            // Retrieve table name and find the appropriate action
            Object tableNameObj = ir.getValue(fieldTableIndex);
            if (tableNameObj==null) {
                logger.logMessage("null-table-name", 10,
                        "Skipped record with NULL table name");
                continue;
            }
            String tableName = tableNameObj.toString().toLowerCase();
            DstTableAction action = actions.get(tableName);
            if (action==null) {
                logger.logMessage("bad-table-name", 10,
                        "Skipped record with unknown table name ["
                                + tableName + "]");
                continue;
            }
            // Retrieve the before and after values
            Object beforeObj = ir.getValue(fieldBeforeIndex);
            Object afterObj = ir.getValue(fieldAfterIndex);
            byte[] before = (beforeObj instanceof byte[]) ?
                    (byte[])beforeObj : null;
            byte[] after = (afterObj instanceof byte[]) ?
                    (byte[])afterObj : null;
            if (before==null && after==null) {
                logger.logMessage("empty-record", 10,
                        "Skipped empty record for table ["
                                + tableName + "]");
                continue;
            }
            // Unpack the before and after values
            value.parse(tableName, before, after);
            // Handle the change
            int rowCount = action.apply(value, connection);
            if (rowCount != 1) {
                logger.logMessage("bad-update-count", 50,
                        "Number of updates for a single record was "
                                + String.valueOf(rowCount)
                                + " for table [" + tableName + "]");
            }
            if (++commitCounter > 1000) {
                connection.commit();
                commitCounter = 0;
            }
        } // while (true)
        // Commit all changes
        if (commitCounter > 0)
            connection.commit();
        { // Print statistics
            final StringBuilder sb = new StringBuilder();
            for (DstTableAction action : actions.values()) {
                if (action.hadChanges()) {
                    if (sb.length() > 0)
                        sb.append("\r\n");
                    sb.append(action.getStatistics());
                }
            }
            if (sb.length() > 0)
                SafeLogger.information(sb.toString());
        }
    }

}
