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
package com.ibm.dsmask.jconf.impl;

import java.util.List;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import com.ibm.dsmask.jconf.beans.*;

/**
 * Configuration database manager.
 * @author zinal
 */
public class DbManager extends DbUtils implements AutoCloseable {

    private static final org.slf4j.Logger LOG = Utils.logger(DbManager.class);

    private static final String[] SQL_CREATE = {
        "CREATE TABLE msk_key("
            + "n VARCHAR(100) NOT NULL UNIQUE,"
            + "v VARCHAR(100) NOT NULL)"
        ,"CREATE TABLE msk_func("
            + "id INTEGER NOT NULL PRIMARY KEY, "
            + "name VARCHAR(100) NOT NULL UNIQUE, "
            + "tcode VARCHAR(30) NOT NULL, "
            + "func_text CLOB NULL)"
        ,"CREATE TABLE msk_rule("
            + "id INTEGER NOT NULL PRIMARY KEY, "
            + "name VARCHAR(100) NOT NULL UNIQUE)"
        ,"CREATE TABLE msk_rule_ctx("
            + "id INTEGER NOT NULL, "
            + "ctx VARCHAR(100) NOT NULL, "
            + "PRIMARY KEY(id, ctx))"
        ,"CREATE TABLE msk_step("
            + "id INTEGER NOT NULL PRIMARY KEY, "
            + "rule_id INTEGER NOT NULL, "
            + "name VARCHAR(100) NOT NULL, "
            + "func_id INTEGER NOT NULL, "
            + "pred_text CLOB NULL, "
            + "pred_lua BOOLEAN NULL, "
            + "uniq_ref VARCHAR(100) NULL, "
            + "uniq_input INTEGER ARRAY NULL, "
            + "uniq_output INTEGER ARRAY NULL, "
            + "UNIQUE(rule_id, name))"
        ,"CREATE TABLE msk_step_arg("
            + "step_id INTEGER NOT NULL, "
            + "step_pos INTEGER NOT NULL, "
            + "ref_id INTEGER NOT NULL, "
            + "ref_pos INTEGER NOT NULL, "
            + "PRIMARY KEY(step_id, step_pos))"
        ,"CREATE TABLE msk_profile("
            + "id INTEGER NOT NULL PRIMARY KEY, "
            + "db_name VARCHAR(100) NOT NULL, "
            + "table_name VARCHAR(200) NOT NULL)"
        ,"CREATE TABLE msk_op("
            + "id INTEGER NOT NULL PRIMARY KEY, "
            + "profile_id INTEGER NOT NULL, "
            + "rule_id INTEGER NOT NULL)"
        ,"CREATE TABLE msk_op_arg("
            + "op_id INTEGER NOT NULL, "
            + "op_pos INTEGER NOT NULL,"
            + "field_name VARCHAR(200) NOT NULL, "
            + "PRIMARY KEY(op_id, op_pos))"
        ,"CREATE TABLE msk_op_out("
            + "op_id INTEGER NOT NULL, "
            + "op_pos INTEGER NOT NULL,"
            + "field_name VARCHAR(200) NOT NULL, "
            + "PRIMARY KEY(op_id, op_pos))"
    };

    private Connection connection = null;

    /**
     * Create a new database, throwing the exception if one exists.
     * @param pathname
     * @throws Exception
     */
    public void create(String pathname) throws Exception {
        final Connection con = DriverManager.getConnection
            (makeConnectionUrl(pathname));
        try {
            con.setAutoCommit(false);
            createTables(con, SQL_CREATE);
            con.commit();
        } catch(Exception ex) {
            try { con.rollback(); } catch(Exception xx) {}
            try { con.close(); } catch(Exception xx) {}
            deleteFiles(pathname);
            throw new Exception("Table creation failed", ex);
        }
        this.connection = con;
    }

    /**
     * Open the existing database, throwing the exception if none exists.
     * @param pathname
     * @throws Exception
     */
    public void open(String pathname) throws Exception {
        final Connection con = DriverManager.getConnection
            (makeConnectionUrl(pathname) + ";IFEXISTS=TRUE");
        try {
            con.setAutoCommit(false);
            checkTables(con);
        } catch(Exception ex) {
            try { con.rollback(); } catch(Exception xx) {}
            try { con.close(); } catch(Exception xx) {}
            throw new Exception("Table check failed", ex);
        }
        this.connection = con;
    }

    @Override
    public void close() {
        if (connection != null) {
            try {
                connection.rollback();
            } catch(Exception ex) {
            }
            try {
                connection.close();
            } catch(Exception ex) {
                LOG.warn("Error closing connection", ex);
            }
            connection = null;
        }
    }

    private void checkTables(Connection con) throws Exception {
        // TODO: implementation
    }

    public void saveKeys(List<MaskingKey> keys) throws Exception {
        if (connection==null)
            throw new IllegalStateException();
        try {
            final String sql = "INSERT INTO msk_key(n, v) VALUES(?,?)";
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                for (MaskingKey mk : keys) {
                    ps.setString(1, mk.getName());
                    ps.setString(2, mk.getValue());
                    ps.addBatch();
                }
                ps.executeBatch();
            }
        } catch(Exception ex) {
            try { connection.rollback(); } catch(Exception xx) {}
            throw new Exception("Cannot save masking keys", ex);
        }
        connection.commit();
    }

    /**
     * Save a single masking operation to the database.
     * Also saves dependent objects, if they were not already saved.
     * Cannot properly update an already-saved object.
     * @param mp
     * @throws Exception
     */
    public void save(MaskingProfile mp) throws Exception {
        if (connection==null)
            throw new IllegalStateException();
        try {
            // save general attributes of the table masking profile
            final int profileId = saveBasicProfile(mp);
            // save masking operations for this table
            for (MaskingOperation mop : mp.getOperations())
                saveOperation(mop, saveRule(mop.getMaskingRule()), profileId);
        } catch(Exception ex) {
            try { connection.rollback(); } catch(Exception xx) {}
            throw new Exception("Cannot save masking profile for table "
                    + "[" + mp.getTableInfo().getName() + "]", ex);
        }
        connection.commit();
    }

    private int saveBasicProfile(MaskingProfile mp) throws Exception {
        PreparedStatement ps = null;
        ResultSet rs = null;
        int profileId = -1;
        try {
            // Look for an already saved rule
            ps = connection.prepareStatement
                ("SELECT id FROM msk_profile WHERE table_name=?");
            ps.setString(1, mp.getTableInfo().getName());
            rs = ps.executeQuery();
            if (rs.next())
                return rs.getInt(1);
            rs.close();  ps.close();
            // Make new rule ID
            ps = connection.prepareStatement("SELECT MAX(id) FROM msk_profile");
            rs = ps.executeQuery();
            profileId = makeNewId(rs);
            rs.close();  ps.close();
            // Store rule row
            ps = connection.prepareStatement("INSERT INTO msk_profile"
                    + "(id, db_name, table_name) VALUES(?,?,?)");
            ps.setInt(1, profileId);
            ps.setString(2, mp.getTableInfo().getDatabase());
            ps.setString(3, mp.getTableInfo().getName());
            ps.executeUpdate();
            ps.close();
        } finally {
            Utils.close(rs);
            Utils.close(ps);
        }
        return profileId;
    }

    private int saveRule(MaskingRule mr) throws Exception {
        PreparedStatement ps = null;
        ResultSet rs = null;
        int ruleId = -1;
        try {
            // Look for an already saved rule
            ps = connection.prepareStatement
                ("SELECT id FROM msk_rule WHERE name=?");
            ps.setString(1, mr.getName());
            rs = ps.executeQuery();
            if (rs.next())
                return rs.getInt(1);
            rs.close();  ps.close();
            // Make new rule ID
            ps = connection.prepareStatement("SELECT MAX(id) FROM msk_rule");
            rs = ps.executeQuery();
            ruleId = makeNewId(rs);
            rs.close();  ps.close();
            // Store rule row
            ps = connection.prepareStatement("INSERT INTO msk_rule(id, name) "
                    + "VALUES(?, ?)");
            ps.setInt(1, ruleId);
            ps.setString(2, mr.getName());
            ps.executeUpdate();
            ps.close();
            // Save contexts
            ps = connection.prepareStatement("INSERT INTO msk_rule_ctx"
                    + "(id, ctx) VALUES(?, ?)");
            boolean hasBatches = false;
            for (String ctx : mr.getContexts()) {
                if (ctx==null || ctx.length()==0)
                    continue;
                ps.setInt(1, ruleId);
                ps.setString(2, ctx);
                ps.addBatch();
                hasBatches = true;
            }
            if (hasBatches)
                ps.executeBatch();
            ps.close();
        } finally {
            Utils.close(rs);
            Utils.close(ps);
        }
        if (!mr.getPipeline().isEmpty()) {
            final StepCtx stepCtx = new StepCtx(mr, ruleId);
            for (MaskingStep step : mr.getPipeline()) {
                int id = saveStep(step, stepCtx);
                step.setIdentifier(id);
                stepCtx.position += 1;
            }
        }
        return ruleId;
    }

    private int saveStep(MaskingStep step, StepCtx stepCtx)
            throws Exception {
        final int funcId = saveFunction(step.getFunction());
        PreparedStatement ps = null;
        ResultSet rs = null;
        int stepId = -1;
        try {
            // Make new step ID
            ps = connection.prepareStatement("SELECT MAX(id) FROM msk_step");
            rs = ps.executeQuery();
            stepId = makeNewId(rs);
            rs.close();  ps.close();
            // Save the step under new ID
            ps = connection.prepareStatement("INSERT INTO msk_step"
                    + "(id, rule_id, name, pred_text, pred_lua, func_id,"
                    + " uniq_ref, uniq_input, uniq_output) "
                    + "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?)");
            ps.setInt(1, stepId);
            ps.setInt(2, stepCtx.ruleId);
            ps.setString(3, step.getName());
            if (step.getPredicate()!=null) {
                ps.setString(4, step.getPredicate().getText());
            } else {
                ps.setString(4, null);
            }
            ps.setBoolean(5, false); // unused PRED_LUA field
            ps.setInt(6, funcId);
            if (step.getUniqCheck()!=null) {
                ps.setString(7, step.getUniqCheck().getProvider());
                ps.setObject(8, convertIndexes(step.getUniqCheck().getInputPositions()));
                ps.setObject(9, convertIndexes(step.getUniqCheck().getOutputPositions()));
            } else {
                ps.setString(7, null);
                ps.setArray(8, null);
                ps.setArray(9, null);
            }
            ps.executeUpdate();
            ps.close();
            // Build and save step arguments
            ps = connection.prepareStatement("INSERT INTO msk_step_arg"
                    + "(step_id, step_pos, ref_id, ref_pos) "
                    + "VALUES(?,?,?,?)");
            int stepPos = 0;
            for (MaskingStep.Ref ref : step.getReferences()) {
                ps.setInt(1, stepId);
                ps.setInt(2, ++stepPos);
                ps.setInt(3, stepCtx.findStep(ref.getName()));
                ps.setInt(4, ref.getPosition());
                ps.addBatch();
            }
            ps.executeBatch();
        } finally {
            Utils.close(rs);
            Utils.close(ps);
        }
        return stepId;
    }

    private static Object[] convertIndexes(int[] indexes) {
        if (indexes==null || indexes.length==0)
            return null;
        final Object[] retval = new Object[indexes.length];
        for (int i=0; i<indexes.length; ++i)
            retval[i] = indexes[i];
        return retval;
    }

    private int saveFunction(MaskingFunction mf) throws Exception {
        PreparedStatement ps = null;
        ResultSet rs = null;
        int funcId = -1;
        try {
            // Look for an already saved rule
            ps = connection.prepareStatement
                ("SELECT id FROM msk_func WHERE name=?");
            ps.setString(1, mf.getName());
            rs = ps.executeQuery();
            if (rs.next())
                return rs.getInt(1);
            rs.close();  ps.close();
            // Make new rule ID
            ps = connection.prepareStatement("SELECT MAX(id) FROM msk_func");
            rs = ps.executeQuery();
            funcId = makeNewId(rs);
            rs.close();  ps.close();
            // Store rule row
            ps = connection.prepareStatement("INSERT INTO msk_func(id, name,"
                    + "tcode, func_text) VALUES(?,?,?,?)");
            ps.setInt(1, funcId);
            ps.setString(2, mf.getName());
            ps.setString(3, mf.getType().name());
            ps.setString(4, mf.getText());
            ps.executeUpdate();
            ps.close();
        } finally {
            Utils.close(rs);
            Utils.close(ps);
        }
        return funcId;
    }

    private int saveOperation(MaskingOperation mop, int ruleId, int profileId)
            throws Exception {
        PreparedStatement ps = null;
        ResultSet rs = null;
        int opId = -1;
        try {
            // Make new operation ID
            ps = connection.prepareStatement("SELECT MAX(id) FROM msk_op");
            rs = ps.executeQuery();
            opId = makeNewId(rs);
            rs.close();  ps.close();
            // Store rule row
            ps = connection.prepareStatement("INSERT INTO msk_op(id, "
                    + "rule_id, profile_id) VALUES(?,?,?)");
            ps.setInt(1, opId);
            ps.setInt(2, ruleId);
            ps.setInt(3, profileId);
            ps.executeUpdate();
            ps.close();
            ps = connection.prepareStatement("INSERT INTO msk_op_arg(op_id, "
                    + "op_pos, field_name) VALUES(?,?,?)");
            int position = 0;
            for (FieldInfo fi : mop.getArguments()) {
                ps.setInt(1, opId);
                ps.setInt(2, ++position);
                ps.setString(3, fi.getName());
                ps.addBatch();
            }
            ps.executeBatch();
            ps.close();
            ps = connection.prepareStatement("INSERT INTO msk_op_out(op_id, "
                    + "op_pos, field_name) VALUES(?,?,?)");
            position = 0;
            for (FieldInfo fi : mop.getOutputs()) {
                ps.setInt(1, opId);
                ps.setInt(2, ++position);
                ps.setString(3, fi.getName());
                ps.addBatch();
            }
            ps.executeBatch();
            ps.close();
        } finally {
            Utils.close(rs);
            Utils.close(ps);
        }
        return opId;
    }

    /**
     * Get next ID from result set fetching MAX(id).
     * Does not allow concurrent modifications of a table.
     * @param rs Result set from SELECT MAX(id) FROM ...
     * @return 1, if the table is empty, and MAX(id)+1 otherwise
     * @throws Exception
     */
    private static int makeNewId(ResultSet rs) throws Exception {
        int id;
        if (rs.next()) {
            id = rs.getInt(1);
            if (rs.wasNull())
                id = 1;
            else
                id += 1;
        } else {
            id = 1;
        }
        return id;
    }

    private static RuntimeException exOutOfOrder(String step, String rule) {
        return new IllegalArgumentException("Out-of-order step "
                            + "reference [" + step
                            + "] in rule [" + rule + "]");
    }

    private static final class StepCtx {
        final MaskingRule mr;
        final int ruleId;
        int position;

        StepCtx(MaskingRule mr, int ruleId) {
            this.mr = mr;
            this.ruleId = ruleId;
            this.position = 0;
        }

        /**
         * Locate step id, specified by step name,
         * in the array of previous step ids.
         * @param name Name of the step
         * @param stepCtx Step processing context
         * @return Step id, referenced by step name
         */
        private int findStep(String name) {
            if (MaskingStep.BASE_NAME.equals(name))
                return 0; // reference to pipeline input vector
            if (name==null || name.length()==0) {
                // without a name we refer to the previous computation
                if (position==0)
                    return 0; // a special case - pipeline input vector
                MaskingStep prevStep = mr.getPipeline().get(position-1);
                if (prevStep.getIdentifier() < 1)
                    throw new IllegalStateException();
                return prevStep.getIdentifier();
            }
            for (MaskingStep step : mr.getPipeline()) {
                if (name.equals(step.getName())) {
                    if (step.getIdentifier() < 1) {
                        throw exOutOfOrder(name, mr.getName());
                    }
                    return step.getIdentifier();
                }
            }
            throw new IllegalArgumentException("Illegal step reference [" + name
                + "] in rule [" + mr.getName() + "]");
        }

    }

}
