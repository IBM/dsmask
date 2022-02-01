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
package com.ibm.dsmask.ops;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import com.ibm.dsmask.beans.MskFunc;
import com.ibm.dsmask.impl.*;

/**
 *
 * @author zinal
 */
public class AlKeyLookup implements AlSimpleVector {

    private final XKeeper keeper;
    private final String dbName;
    private final String tableName;
    private final String userName;
    private final String[] inFields;
    private final String[] outFields;
    private final int[] indexes;

    private Connection connection = null;
    private PreparedStatement findStmt = null;

    public AlKeyLookup(XKeeper ctx, MskFunc func) {
        this.keeper = ctx;
        List<String[]> ops = Utils.parseConfig(func.getText());
        this.dbName = Utils.getConfigValue(ops, "db");
        if (Utils.hasConfigEntry(ops, "user")) {
            this.userName = Utils.getConfigValue(ops, "user");
        } else {
            this.userName = "";
        }
        this.tableName = Utils.getConfigValue(ops, "table");
        this.inFields = Utils.getConfigArgs(ops, "keys");
        this.outFields = Utils.getConfigArgs(ops, "out");
        if (Utils.hasConfigEntry(ops, "indexes")) {
            this.indexes = Utils.parseIndexes(
                    Utils.getConfigValue(ops, "indexes"));
        } else {
            this.indexes = null;
        }
        if (indexes!=null && indexes.length!=inFields.length) {
            throw new IllegalArgumentException("Function ["
                    + func.getName() + "] has bad indexes: "
                    + indexes.length + " vs " + inFields.length);
        }
    }

    @Override
    public XVector exec(XVector in, XVector out) {
        if (in.values.length < inFields.length) {
            throw new AlgoInitException("Short input: need "
                    + inFields.length + ", have only "
                    + in.values.length + " values");
        }
        try {
            out = XVector.make(out, outFields.length);
            boolean haveNull = false;
            for (int i=0; i<inFields.length; ++i) {
                if (in.values[i] == null)
                    haveNull = true;
            }
            if (haveNull) {
                for (int i=0; i<outFields.length; ++i)
                    out.values[i] = null;
            } else {
                if (findStmt==null) {
                    try {
                        connection = keeper.openDictionary(dbName, userName);
                        findStmt = connection.prepareStatement(buildSql());
                        keeper.registerStatement(findStmt);
                    } catch(Exception ex) {
                        // Statements and connections are closed by the Keeper
                        throw new AlgoInitException("Failed to open key dictionary "
                                + dbName + "." + tableName, ex);
                    }
                }
                if (indexes==null) {
                    for (int i=0; i<inFields.length; ++i) {
                        findStmt.setObject(i+1, in.values[i]);
                    }
                } else {
                    for (int i=0; i<indexes.length; ++i) {
                        findStmt.setObject(i+1, in.values[indexes[i]-1]);
                    }
                }
                try (ResultSet rs = findStmt.executeQuery()) {
                    if (rs.next()) {
                        for (int i=0; i<outFields.length; ++i)
                            out.values[i] = rs.getObject(i+1);
                    } else {
                        for (int i=0; i<outFields.length; ++i)
                            out.values[i] = null;
                    }
                }
            }
        } catch(Exception ex) {
            throw new AlgoInitException("Error fetching from key dictionary "
                    + dbName + "." + tableName, ex);
        }
        return out;
    }

    private String buildSql() {
        final StringBuilder sql = new StringBuilder();
        sql.append("SELECT ");
        boolean comma = false;
        for (String field : outFields) {
            if (comma)
                sql.append(", ");
            sql.append(field);
            comma = true;
        }
        sql.append(" FROM ");
        sql.append(tableName);
        sql.append(" WHERE ");
        boolean needsep = false;
        for (String field : inFields) {
            if (needsep)
                sql.append(" AND ");
            else
                needsep = true;
            sql.append(field);
            sql.append("=?");
        }
        return sql.toString();
    }

    @Override
    public boolean isIterationsSupported() {
        return false;
    }

    @Override
    public XVector exec(XVector in, XVector out, int iteration) {
        return exec(in, out);
    }

}
