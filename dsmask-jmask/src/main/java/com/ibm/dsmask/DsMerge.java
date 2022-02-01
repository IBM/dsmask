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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import com.ibm.is.cc.javastage.api.*;
import com.ibm.dsmask.apply.*;

/**
 * DataStage column merging operator.
 * This operator allows data from multiple tables to be merged
 * into a single commit-sequence stream, sorted over transaction
 * and row numbers, and then applied over the target database.
 * @author mzinal
 */
public class DsMerge extends Processor {

    public static final String PROP_FIELDS_MAP = "FieldsMap";
    public static final String PROP_TABLE_NAME = "TableName";
    public static final String PROP_TABLE_NAME_FIELD = "TableNameField";
    public static final String PROP_OP_SEQ_FIELD = "OpSeqField";

    private String tableName;
    private String tableNameField;
    // group prefix -> target field name
    private final Map<String, String> propFieldsMap = new HashMap<>();
    // target field name -> target group definition
    private final Map<String, DstMergeGroup> targetGroups = new HashMap<>();
    // source field index -> target field index
    private final Map<Integer, Integer> directMap = new HashMap<>();
    // Target OP_SEQ field
    private String targetOpSeqName;
    // Possible source OP_SEQ fields
    private List<String> sourceOpSeqNames;

    private int tableNameIndex = -1;
    private int targetOpSeqIndex = -1;
    private List<Integer> sourceOpSeqIndexes;

    private final List<String> configErrors = new ArrayList<>();

    private InputLink inputLink;
    private OutputLink outputLink;

    @Override
    public List<PropertyDefinition> getUserPropertyDefinitions() {
        List<PropertyDefinition> propList = new ArrayList<>();
        propList.add(new PropertyDefinition(PROP_TABLE_NAME, "",
                "Table name",
                "Table name to be included in the merged records",
                PropertyDefinition.Scope.STAGE));
        propList.add(new PropertyDefinition(PROP_TABLE_NAME_FIELD, "",
                "Table name field",
                "Field name on target to store the table name",
                PropertyDefinition.Scope.STAGE));
        propList.add(new PropertyDefinition(PROP_FIELDS_MAP, "",
                "Fields map",
                "Maps field name prefix to the target field name",
                PropertyDefinition.Scope.STAGE));
        propList.add(new PropertyDefinition(PROP_OP_SEQ_FIELD, "",
                "Operational sequence fields",
                "Destination and possible source field names "
                        + "for operational sequence number",
                PropertyDefinition.Scope.STAGE));
        return propList;
    }

    @Override
    public Capabilities getCapabilities() {
      Capabilities capabilities = new Capabilities();
      // we need the input data to merge
      capabilities.setMinimumInputLinkCount(1);
      capabilities.setMaximumInputLinkCount(1);
      // we need to write the merged data
      capabilities.setMinimumOutputStreamLinkCount(1);
      capabilities.setMaximumOutputStreamLinkCount(1);
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
        tableName = props.getProperty(PROP_TABLE_NAME);
        if (tableName==null || tableName.length()==0)
            addConfError(PROP_TABLE_NAME, "Must be defined");
        tableNameField = props.getProperty(PROP_TABLE_NAME_FIELD);
        if (tableNameField==null || tableNameField.length()==0)
            tableNameField = "TABLE_NAME";
        // Parse the fields map passed as parameter
        // Examples:
        //   BEFORE_:BEFORE_VAL,AFTER_:AFTER_VAL
        //   BEFORE_,AFTER_
        String theFieldsMap = props.getProperty(PROP_FIELDS_MAP);
        if (theFieldsMap == null || theFieldsMap.isEmpty()) {
            addConfError(PROP_FIELDS_MAP, "Must be defined");
        } else {
            for (String theItem : theFieldsMap.split("[,]")) {
                final String key, value;
                if (theItem.contains(":")) {
                    String[] parts = theItem.split("[:]");
                    key = parts[0].trim();
                    value = parts[1].trim();
                } else {
                    key = theItem.trim().toLowerCase();
                    value = key.endsWith("_") ? (key + "VAL") : (key + "_VAL");
                }
                if (key.length() > 0 && value.length() > 0)
                    propFieldsMap.put(key.toLowerCase(), value.toLowerCase());
                else
                    addConfError(PROP_FIELDS_MAP, "Illegal (empty) entries");
            }
        }
        // Parse the list of OP_SEQ field name strings
        String opSeqLine = props.getProperty(PROP_OP_SEQ_FIELD);
        if (opSeqLine==null || opSeqLine.length()==0)
            opSeqLine = "OP_SEQ,BEFORE_OP_SEQ,AFTER_OP_SEQ";
        targetOpSeqName = null;
        if (opSeqLine.trim().length() > 0) {
            String[] parts = opSeqLine.split("[,]");
            for (String part : parts) {
                String fieldName = part.trim();
                if (fieldName.length() > 0) {
                    if (targetOpSeqName == null)
                        targetOpSeqName = fieldName;
                    else {
                        if (sourceOpSeqNames == null)
                            sourceOpSeqNames = new ArrayList<>();
                        sourceOpSeqNames.add(fieldName);
                    }
                }
            }
        }
        if (targetOpSeqName!=null) {
            if (sourceOpSeqNames==null)
                sourceOpSeqNames = new ArrayList<>();
            if ( sourceOpSeqNames.isEmpty() )
                sourceOpSeqNames.add(targetOpSeqName);
        }

        if (runtime) {
            inputLink = config.getInputLink(0);
            outputLink = config.getOutputLink(0);
            // Prepare the target groups
            for (Map.Entry<String,String> me : propFieldsMap.entrySet()) {
                DstMergeGroup tg = targetGroups.get(me.getValue());
                if (tg==null) {
                    tg = new DstMergeGroup(me.getValue(),
                             findColumn(outputLink.getColumnMetadata(),
                                     me.getValue()) );
                    if (tg.getTargetColumnIndex() < 0)
                        configErrors.add(me.getValue() + ": missing output column");
                    // we add even invalid columns to suppress multiple reporting
                    targetGroups.put(me.getValue(), tg);
                }
            }
            // Map the input columns to the target groups
            for ( ColumnMetadata inCol : inputLink.getColumnMetadata() ) {
                mapInputColumn(inCol);
            }
            // Look for the table name column in the output
            for (ColumnMetadata outCol : outputLink.getColumnMetadata()) {
                if (outCol.getName().equalsIgnoreCase(tableNameField)) {
                    tableNameIndex = outCol.getIndex();
                    break;
                }
            }
            if (tableNameIndex<0) {
                addConfError(PROP_TABLE_NAME_FIELD, "Cannot find field ["
                        + tableNameField + "] for the target link");
            }
            // Map the OP_SEQ, if defined
            if (targetOpSeqName!=null) {
                targetOpSeqIndex = findColumn(outputLink.getColumnMetadata(),
                        targetOpSeqName);
                if (targetOpSeqIndex > 0) {
                    for (String cname : sourceOpSeqNames) {
                        int index = findColumn(inputLink.getColumnMetadata(), cname);
                        if (index > 0) {
                            if (sourceOpSeqIndexes==null)
                                sourceOpSeqIndexes = new ArrayList<>();
                            sourceOpSeqIndexes.add(index);
                        }
                    }
                }
                if (sourceOpSeqIndexes==null || sourceOpSeqIndexes.isEmpty())
                    targetOpSeqIndex = -1;
            }
        }

        return configErrors.isEmpty();
    }

    private int findColumn(List<ColumnMetadata> meta, String colName) {
        for (ColumnMetadata cm : meta) {
            if (cm.getName().equalsIgnoreCase(colName)) {
                return cm.getIndex();
            }
        }
        return -1;
    }

    private boolean mapInputColumn(ColumnMetadata inCol) {
        // Check for possible prefix group mapping
        for (Map.Entry<String,String> me : propFieldsMap.entrySet()) {
            if ( inCol.getName().toLowerCase().startsWith(me.getKey()) ) {
                DstMergeGroup mg = targetGroups.get(me.getValue());
                if (mg!=null) {
                    mg.addColumn(inCol, me.getKey());
                    return true;
                }
            }
        }
        // Check for direct source-to-target mapping
        for ( ColumnMetadata outCol : outputLink.getColumnMetadata() ) {
            if (outCol.getName().equalsIgnoreCase(inCol.getName())) {
                directMap.put(inCol.getIndex(), outCol.getIndex());
                return true;
            }
        }
        // No mapping in groups, no mapping directly
        return false;
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
    public void process() throws Exception {
        while (true) {
            InputRecord ir = inputLink.readRecord();
            if (ir==null)
                break; // no more input
            OutputRecord or = outputLink.getOutputRecord();
            or.setValue(tableNameIndex, tableName);
            // Copying the direct fields
            for (Map.Entry<Integer, Integer> me : directMap.entrySet()) {
                or.setValue(me.getValue(), ir.getValue(me.getKey()));
            }
            // Building the group fields
            for (DstMergeGroup mg : targetGroups.values()) {
                final Map<String, Object> data = DstMergeAlgo.collect(ir, mg);
                final byte[] value;
                if (data==null || data.isEmpty()) {
                    value = null;
                } else {
                    value = DstMergeAlgo.pack(data);
                }
                or.setValue(mg.getTargetColumnIndex(), value);
            }
            // Build the OP_SEQ value
            if (targetOpSeqIndex > 0) {
                Object opSeqVal = null;
                for (Integer index : sourceOpSeqIndexes) {
                    Object cur = ir.getValue(index);
                    if (cur!=null) {
                        opSeqVal = cur;
                        break;
                    }
                }
                or.setValue(targetOpSeqIndex, opSeqVal);
            }
            outputLink.writeRecord(or);
        }
    }

}
