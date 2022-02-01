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
package com.ibm.dsmask.apply;

import java.sql.Connection;
import java.sql.PreparedStatement;

/**
 * Implementation of actions to apply changes to the destination table
 * for the DsApply operator.
 * @author mzinal
 */
public class DstTableAction implements AutoCloseable {

    private final DstTableConf config;

    private int insertCount = 0;
    private int updateCount = 0;
    private int deleteCount = 0;

    private transient PreparedStatement sqlInsert = null;
    private transient PreparedStatement sqlDelete = null;
    private transient PreparedStatement sqlUpdate = null;

    public DstTableAction(DstTableConf config) {
        this.config = config;
    }

    public DstTableConf getConfig() {
        return config;
    }

    public int getInsertCount() {
        return insertCount;
    }

    public int getUpdateCount() {
        return updateCount;
    }

    public int getDeleteCount() {
        return deleteCount;
    }

    @Override
    public void close() {
        close(sqlInsert);  sqlInsert = null;
        close(sqlDelete);  sqlDelete = null;
        close(sqlUpdate);  sqlUpdate = null;
    }

    public static void close(PreparedStatement ps) {
        if (ps!=null)
            try { ps.close(); } catch(Exception ex) {}
    }

    /**
     * Apply the changes to the destination table
     * @param value Changes data
     * @param con Target database connection
     * @return Number of rows inserted/updated/deleted
     * @throws Exception In case of any error
     */
    public int apply(DstRowValue value, Connection con) throws Exception {
        if (!value.getTableName().equalsIgnoreCase(config.getCommonName())) {
            throw new IllegalArgumentException("DstTableAction.apply() expects ["
                    + config.getCommonName() + "] table and got row for ["
                    + value.getTableName() + "]");
        }
        updateStatements(con);
        if (value.getAfter().isEmpty()) {
            // no after value - should have a before one, this is DELETE
            return applyDelete(value);
        }
        if (value.getBefore().isEmpty()) {
            // has after, but no before - INSERT
            return applyInsert(value);
        } else {
            // has both before and after - UPDATE
            return applyUpdate(value);
        }
    }

    public boolean hadChanges() {
        return (insertCount + deleteCount + updateCount) > 0;
    }

    public String getStatistics() {
        return config.getDestinationName()
                + ": " + String.valueOf(insertCount) + " inserted"
                + ", " + String.valueOf(deleteCount) + " deleted"
                + ", " + String.valueOf(updateCount) + " updated"
                ;
    }

    private void updateStatements(Connection con)
            throws Exception {
        if (sqlUpdate==null) {
            close();
            sqlInsert = makeInsert(con);
            sqlDelete = makeDelete(con);
            sqlUpdate = makeUpdate(con);
        }
    }

    private PreparedStatement makeInsert(Connection con) throws Exception {
        final StringBuilder sql = new StringBuilder();
        sql.append("INSERT INTO ")
                .append(config.getDestinationName()).append("(");
        boolean needComma = false;
        for (String field : config.getAllFields()) {
            if (needComma) sql.append(","); else needComma = true;
            sql.append(field);
        }
        sql.append(") VALUES (");
        needComma = false;
        for (String field : config.getAllFields()) {
            if (needComma) sql.append(","); else needComma = true;
            sql.append("?");
        }
        sql.append(")");
        return con.prepareStatement(sql.toString());
    }

    private int applyInsert(DstRowValue value) throws Exception {
        if (value.getAfter().isEmpty())
            return 0; // nothing to do
        int index = 0;
        for (String field : config.getAllFields()) {
            sqlInsert.setObject(++index, value.getAfterVal(field));
        }
        int retval = sqlInsert.executeUpdate();
        insertCount += retval;
        return retval;
    }

    private PreparedStatement makeDelete(Connection con) throws Exception {
        final StringBuilder sql = new StringBuilder();
        sql.append("DELETE FROM ")
                .append(config.getDestinationName()).append(" WHERE ");
        boolean needComma = false;
        for (String field : config.getKeyFields()) {
            if (needComma) sql.append(" AND "); else needComma = true;
            sql.append(field).append("=?");
        }
        return con.prepareStatement(sql.toString());
    }

    private int applyDelete(DstRowValue value) throws Exception {
        if (value.getBefore().isEmpty())
            return 0; // nothing to do
        int index = 0;
        for (String fn : config.getKeyFields()) {
            sqlDelete.setObject(++index, value.getBeforeVal(fn));
        }
        int retval = sqlDelete.executeUpdate();
        deleteCount += retval;
        return retval;
    }

    private PreparedStatement makeUpdate(Connection con) throws Exception {
        final StringBuilder sql = new StringBuilder();
        sql.append("UPDATE ")
                .append(config.getDestinationName()).append(" SET ");
        boolean needComma = false;
        for (String field : config.getAllFields()) {
            if (needComma) sql.append(","); else needComma = true;
            sql.append(field).append("=?");
        }
        sql.append(" WHERE ");
        needComma = false;
        for (String field : config.getKeyFields()) {
            if (needComma) sql.append(" AND "); else needComma = true;
            sql.append(field).append("=?");
        }
        return con.prepareStatement(sql.toString());
    }

    private int applyUpdate(DstRowValue value) throws Exception {
        if (value.getBefore().isEmpty() || value.getAfter().isEmpty())
            return 0; // nothing to do
        int index = 0;
        for (String field : config.getAllFields()) {
            sqlUpdate.setObject(++index, value.getAfterVal(field));
        }
        for (String field : config.getKeyFields()) {
            sqlUpdate.setObject(++index, value.getBeforeVal(field));
        }
        int retval = sqlUpdate.executeUpdate();
        updateCount += retval;
        return retval;
    }

}
