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
package com.ibm.dsmask.impl;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.ibm.dsmask.GlobalNames;
import com.ibm.dsmask.beans.*;

/**
 * Algorithm of loading masking context from the configuration database.
 * @author zinal
 */
public class ContextLoader implements AutoCloseable {

    private final Connection connection;
    private final Map<String, PreparedStatement> statements = new HashMap<>();

    public ContextLoader() throws Exception {
        this(GlobalNames.DEF_CONFIG);
    }

    public ContextLoader(String configName) throws Exception {
        this(System.getenv(GlobalNames.ENV_CONFIG), configName);
    }

    public ContextLoader(String configPath, String configName) throws Exception {
        if (configPath==null)
            throw new Exception("Configuration path not specified (DSMASK_CONFIG_PATH)");
        configName = Utils.lower(configName);
        final String dbPath = new File(new File(configPath), configName)
                .getAbsolutePath();
        final Connection temp = DriverManager.getConnection
            ("jdbc:h2:" + dbPath.replaceAll("\\\\", "/")
            + ";ACCESS_MODE_DATA=R");
        try {
            temp.setAutoCommit(false);
            temp.setReadOnly(true);
        } catch(Exception ex) {
            Utils.close(temp);
            throw new Exception("Cannot open configuration database", ex);
        }
        this.connection = temp;
    }

    public MskContext load(String databaseName, String tableName,
                Collection<String> scopePrefixes)
            throws Exception {
        final MskContext context = new MskContext();
        context.getTable().setDatabaseName(databaseName);
        context.getTable().setTableName(tableName);
        select("SELECT id FROM msk_profile WHERE table_name=? "
                + ((databaseName==null) ? "" : "AND db_name=?"),
                new Handler() {
            @Override
            public void assign(PreparedStatement ps) throws Exception {
                ps.setString(1, Utils.lower(tableName));
                if (databaseName!=null)
                    ps.setString(2, Utils.lower(databaseName));
            }
            @Override
            public void handle(ResultSet rs) throws Exception {
                if (!rs.next())
                    context.getTable().setId(-1);
                else
                    context.getTable().setId(rs.getInt(1));
            }
                });
        if (context.getTable().getId() < 1)
            return null; // no data for the specified table
        loadKeys(context);
        List<MskOp> dstOperations = context.getTable().getOperations();
        List<MskOp> operations = loadOperations(context);
        if (dstOperations.isEmpty()==false)
            throw new Exception("Logic error: operations already filled?");
        if (operations.isEmpty())
            throw new Exception("Incorrect input config: empty masking context");
        if (scopePrefixes==null || scopePrefixes.isEmpty()) {
            // In case there are no scope prefixes defined, we work as before
            dstOperations.addAll(operations);
        } else {
            // For each scope prefix we need to define a separate
            // set of masking actions.
            for (String scopePrefix : scopePrefixes) {
                for (MskOp op : operations) {
                    dstOperations.add(new MskOp(op, scopePrefix));
                }
            }
        }
        return context;
    }

    @Override
    public void close() {
        for (PreparedStatement ps : statements.values())
            Utils.close(ps);
        statements.clear();
        Utils.close(connection);
    }

    private void loadKeys(MskContext context) throws Exception {
        select("SELECT n, v FROM msk_key",
                new Handler() {
            @Override
            public void assign(PreparedStatement ps) throws Exception {
                /*noop*/
            }
            @Override
            public void handle(ResultSet rs) throws Exception {
                while (rs.next()) {
                    final MskKey key = new MskKey();
                    key.setName(rs.getString(1));
                    key.setValue(rs.getString(2));
                    context.addKey(key);
                }
            }
        });
    }

    private List<MskOp> loadOperations(MskContext context) throws Exception {
        final List<MskOp> retval = new ArrayList<>();
        select("SELECT id, rule_id FROM msk_op WHERE profile_id=?",
                new Handler() {
            @Override
            public void assign(PreparedStatement ps) throws Exception {
                ps.setInt(1, context.getTable().getId());
            }
            @Override
            public void handle(ResultSet rs) throws Exception {
                while (rs.next()) {
                    final MskOp op = new MskOp();
                    op.setId(rs.getInt(1));
                    op.setRule(loadRule(context, rs.getInt(2)));
                    op.setTable(context.getTable());
                    loadOperationInputs(op);
                    loadOperationOutputs(op);
                    retval.add(op);
                }
            }
        });
        return retval;
    }

    private void loadOperationInputs(MskOp op) throws Exception {
        select("SELECT field_name FROM msk_op_arg "
                + "WHERE op_id=? ORDER BY op_pos",
                new Handler() {
            @Override
            public void assign(PreparedStatement ps) throws Exception {
                ps.setInt(1, op.getId());
            }
            @Override
            public void handle(ResultSet rs) throws Exception {
                while (rs.next()) {
                    op.getInputFields().add(rs.getString(1));
                }
            }
        });
    }

    private void loadOperationOutputs(MskOp op) throws Exception {
        select("SELECT field_name FROM msk_op_out "
                + "WHERE op_id=? ORDER BY op_pos",
                new Handler() {
            @Override
            public void assign(PreparedStatement ps) throws Exception {
                ps.setInt(1, op.getId());
            }
            @Override
            public void handle(ResultSet rs) throws Exception {
                while (rs.next()) {
                    op.getOutputFields().add(rs.getString(1));
                }
            }
        });
    }

    private MskRule loadRule(MskContext context, int ruleId) throws Exception {
        final MskRule cachedRule = context.getRule(ruleId);
        if (cachedRule!=null)
            return cachedRule;
        final MskRule rule = new MskRule();
        rule.setId(ruleId);
        select("SELECT name FROM msk_rule WHERE id=?",
                new Handler() {
            @Override
            public void assign(PreparedStatement ps) throws Exception {
                ps.setInt(1, ruleId);
            }
            @Override
            public void handle(ResultSet rs) throws Exception {
                if (rs.next())
                    rule.setName(rs.getString(1));
                else
                    throw new IllegalStateException("db: no rule #"+ruleId);
            }
        });
        if (rule.getName()==null)
            return null;
        loadSteps(context, rule);
        context.addRule(rule);
        return rule;
    }

    private void loadSteps(final MskContext context, final MskRule rule)
            throws Exception {
        select("SELECT id, name, func_id, pred_text, pred_lua, "
                + "uniq_ref, uniq_input, uniq_output FROM msk_step "
                + "WHERE rule_id=? ORDER BY id",
                new Handler() {
            @Override
            public void assign(PreparedStatement ps) throws Exception {
                ps.setInt(1, rule.getId());
            }
            @Override
            public void handle(ResultSet rs) throws Exception {
                int position = 0;
                while (rs.next()) {
                    final MskStep step = new MskStep(rule, ++position);
                    step.setId(rs.getInt(1));
                    step.setName(rs.getString(2));
                    step.setFunction(loadFunction(context, rs.getInt(3)));
                    String predText = rs.getString(4);
                    if (predText!=null) {
                        // empty text becomes null to allow simpler checks
                        // for predicate existence
                        predText = predText.trim();
                        if (predText.length()==0)
                            predText = null;
                    }
                    step.setPredicateText(predText);
                    // rs.getBoolean(5) - unused PRED_LUA field
                    String uniqRef = rs.getString(6);
                    if (uniqRef != null && uniqRef.length() > 0) {
                        final MskUniq uniq = new MskUniq();
                        uniq.setProvider(uniqRef);
                        uniq.setInputPositions(convertPositions( rs.getArray(7) ));
                        uniq.setOutputPositions(convertPositions( rs.getArray(8) ));
                        step.setUniqCheck(uniq);
                    }
                    loadRefs(rule, step);
                    rule.getSteps().add(step);
                }
            }
        });
    }

    private static int[] convertPositions(java.sql.Array array) throws Exception {
        if (array==null)
            return null;
        Object[] indexes = (Object[]) array.getArray();
        if (indexes==null || indexes.length==0)
            return null;
        final int[] retval = new int[indexes.length];
        for (int i=0; i<indexes.length; ++i) {
            if (indexes[i] == null)
                retval[i] = 0;
            else if (indexes[i] instanceof Number)
                retval[i] = ((Number) indexes[i]).intValue();
            else
                retval[i] = Integer.parseInt(indexes[i].toString());
        }
        return retval;
    }

    private void loadRefs(MskRule rule, MskStep step) throws Exception {
        select("SELECT ref_id, ref_pos FROM msk_step_arg "
                + "WHERE step_id=? ORDER BY step_pos",
                new Handler() {
            @Override
            public void assign(PreparedStatement ps) throws Exception {
                ps.setInt(1, step.getId());
            }
            @Override
            public void handle(ResultSet rs) throws Exception {
                while (rs.next()) {
                    final MskRef ref = new MskRef();
                    int parentIndex = findParent(rule, rs.getInt(1));
                    ref.setParentIndex(parentIndex);
                    if (parentIndex >= 0)
                        ref.setParent(rule.getSteps().get(parentIndex));
                    ref.setPosition(rs.getInt(2));
                    step.getRefs().add(ref);
                }
            }
        });
    }

    private static int findParent(MskRule rule, int stepId) {
        if (stepId==0)
            return -1; // reference to "base" values (rule input fields)
        int index = 0;
        for (MskStep step : rule.getSteps()) {
            if (stepId==step.getId())
                return index;
            ++index;
        }
        throw new IllegalArgumentException("No step #" + stepId + " in rule #"
                + rule.getId());
    }

    private MskFunc loadFunction(MskContext context, int funcId)
            throws Exception {
        final MskFunc cachedFunc = context.getFunction(funcId);
        if (cachedFunc!=null)
            return cachedFunc;
        final MskFunc func = new MskFunc();
        func.setId(funcId);
        select("SELECT name, tcode, func_text FROM msk_func WHERE id=?",
                new Handler() {
            @Override
            public void assign(PreparedStatement ps) throws Exception {
                ps.setInt(1, funcId);
            }
            @Override
            public void handle(ResultSet rs) throws Exception {
                if (!rs.next())
                    throw new IllegalArgumentException("Unknown masking function #" + funcId);
                func.setName(rs.getString(1));
                func.setFunctionType(FunctionType.fromCode(rs.getString(2)));
                func.setText(rs.getString(3));
            }
        });
        context.addFunction(func);
        switch (func.getFunctionType()) {
            case FPE:
            case HashLookup:
                // These function types are always iterable.
                func.setIterable(true);
                break;
            default: /*noop*/
                break;
        }
        return func;
    }

    private void select(String text, Handler handler) throws Exception {
        PreparedStatement ps = statements.get(text);
        if (ps==null) {
            ps = connection.prepareStatement(text);
            statements.put(text, ps);
        }
        handler.assign(ps);
        final ResultSet rs = ps.executeQuery();
        try {
            handler.handle(rs);
        } finally {
            Utils.close(rs);
        }
    }

    private static interface Handler {
        void assign(PreparedStatement ps) throws Exception;
        void handle(ResultSet rs) throws Exception;
    }

}
