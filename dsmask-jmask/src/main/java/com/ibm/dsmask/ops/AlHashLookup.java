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
import org.apache.commons.collections4.map.LRUMap;
import com.ibm.dsmask.impl.*;
import com.ibm.dsmask.beans.MskFunc;
import com.ibm.dsmask.algo.BasicHasher;

/**
 * Find substitution values in a hash table.
 * Hash is computed as a CRC-32 value over inputs (or subset of inputs).
 * @author zinal
 */
public class AlHashLookup implements AlSimpleVector {

    private final XKeeper keeper;
    private final String dbName;
    private final String idField;
    private final String tableName;
    private final String userName;
    private final String[] outFields;
    private final String userKey;
    private final int[] indexes;

    private Connection connection = null;
    private PreparedStatement findStmt = null;
    private long maxHash = -1;
    private BasicHasher hasher = null;

    private final LRUMap<Long, XVector> valueCache = new LRUMap<>(10000);

    public AlHashLookup(XKeeper ctx, MskFunc function) {
        this.keeper = ctx;
        List<String[]> ops = Utils.parseConfig(function.getText());
        this.dbName = Utils.getConfigValue(ops, "db");
        if (Utils.hasConfigEntry(ops, "user")) {
            this.userName = Utils.getConfigValue(ops, "user");
        } else {
            this.userName = "";
        }
        this.tableName = Utils.getConfigValue(ops, "table");
        this.idField = Utils.getConfigValue(ops, "id");
        this.outFields = Utils.getConfigArgs(ops, "out");
        this.userKey = ctx.getUserKey(Utils.getConfigValue(ops, "key"));
        if (Utils.hasConfigEntry(ops, "indexes")) {
            // Indexes are used to support extra parameters which are used by the predicates,
            // but are excluded from hash computation.
            // TODO: Ideally predicate computation should be done separately, and produce
            // special flags, which could afterwards be used to filter out unnecessary
            // computations.
            this.indexes = Utils.parseIndexes(
                    Utils.getConfigValue(ops, "indexes"));
        } else {
            this.indexes = null;
        }
    }

    @Override
    public boolean isIterationsSupported() {
        return true;
    }

    @Override
    public XVector exec(XVector in, XVector out) {
        return exec(in, out, 0);
    }

    @Override
    public XVector exec(XVector in, XVector out, int iteration) {
        if (in.isAllNull()) {
            // Null input -> null output
            out = XVector.make(out, outFields.length);
            out.clear();
            return out;
        }

        // We need the SELECT statement
        if (findStmt==null)
            try {
                connection = keeper.openDictionary(dbName, userName);
                maxHash = queryHashSize();
                findStmt = connection.prepareStatement(buildSql());
                keeper.registerStatement(findStmt);
            } catch(Exception ex) {
                // Statements and connections are closed by the Keeper
                throw new AlgoInitException("Failed to open hash dictionary "
                        + dbName + "." + tableName, ex);
            }

        // Try to grab the value from the cache
        final long hashVal = calcHash(in, iteration);
        { // Perform cache lookup
            XVector val = valueCache.get(hashVal);
            if (val!=null) {
                // Return a cached value, no need to query DB
                return XVector.inPlaceCopy(out, val);
            }
        }

        try {
            findStmt.setLong(1, hashVal);
            try (ResultSet rs = findStmt.executeQuery()) {
                if (!rs.next()) {
                    throw new AlgoExecException("Missing value for "
                            + "key=" + String.valueOf(hashVal)
                            + ", total=" + String.valueOf(maxHash)
                    );
                }
                out = XVector.make(out, outFields.length);
                for (int i=0; i<outFields.length; ++i)
                    out.values[i] = rs.getObject(i+1);
            }
        } catch(Exception ex) {
            throw new AlgoInitException("Error fetching from hash dictionary "
                    + dbName + "." + tableName, ex);
        }

        { // Put the value to the cache
            valueCache.put(hashVal, new XVector(out));
        }

        return out;
    }

    /**
     * Get the hash size from the database
     * @return Max id field + 1
     * @throws Exception
     */
    private long queryHashSize() throws Exception {
        final String sql = "SELECT MAX(" + idField + ") FROM " + tableName;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                long value = rs.getLong(1);
                if (rs.wasNull())
                    return 0L;
                return value + 1L;
            }
        }
    }

    /**
     * Compute the hash value normalized to hash size
     * @param vec Input data vector
     * @param iteration Current iteration number
     * @return hash value (remainder of division to max id)
     */
    protected final long calcHash(XVector vec, int iteration) {
        if (hasher==null) {
            hasher = new BasicHasher(userKey, indexes);
        }
        final long value = hasher.calcHash(vec.values, iteration);
        return value % maxHash;
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
        sql.append(idField);
        sql.append("=?");
        return sql.toString();
    }

}
