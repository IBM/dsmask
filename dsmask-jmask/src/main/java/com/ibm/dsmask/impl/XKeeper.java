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

import net.dsmask.algo.CharClassSet;
import net.dsmask.algo.CharzTable;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.ibm.dsmask.beans.*;
import com.ibm.dsmask.ops.*;

/**
 * Masking context keeper, containing the main resources used
 * by the masking operator and responsible for releasing all
 * the allocated resources after the operator completes running.
 * Provides access to Lua and Groovy runners, ODPP runner,
 * masking configuration and masking dictionaries.
 *
 * @author zinal
 */
public class XKeeper implements AutoCloseable {

    private final MskContext context;
    private final XServices services;

    private final List<PreparedStatement> statements = new ArrayList<>();
    private final List<Connection> connections = new ArrayList<>();

    private final Map<String, CharClassSet> charClassSets = new HashMap<>();
    private final Map<String, CharzTable> charTables = new HashMap<>();
    private final Map<Integer, XExecutor> executors = new HashMap<>();
    private final Map<Integer, XExecutor> predicates = new HashMap<>();

    public XKeeper(MskContext maskingContext, XServices services) {
        this.context = maskingContext;
        this.services = services;
    }

    public MskContext getContext() {
        return context;
    }

    public XServices getServices() {
        return services;
    }

    public MskTable getTable() {
        return context.getTable();
    }

    public CharClassSet getCharClassSet(String name) {
        CharClassSet ccs = charClassSets.get(name);
        if (ccs==null) {
            try {
                ccs = CharClassSet.load(context.getDictPath(), name);
                charClassSets.put(name, ccs);
            } catch(Exception ex) {
                throw new RuntimeException("Cannot load char class set " + name, ex);
            }
        }
        return ccs;
    }

    public CharzTable getCharTable(String name) {
        CharzTable tab = charTables.get(name);
        if (tab==null) {
            try {
                tab = CharzTable.load(context.getDictPath(), name);
                charTables.put(name, tab);
            } catch(Exception ex) {
                throw new RuntimeException("Cannot load character translation table " + name, ex);
            }
        }
        return tab;
    }

    public Connection openDictionary(String name, String userName) {
        return doOpenDict(context.getDictPath(), name, userName);
    }

    private Connection doOpenDict(String dictPath,
            String name, String userName) {
        final String dbPath = new File(new File(dictPath), name)
                .getAbsolutePath();
        Connection temp = null;
        try {
            temp = DriverManager.getConnection
                ("jdbc:h2:" + dbPath.replaceAll("\\\\", "/")
                    + ";ACCESS_MODE_DATA=R", userName, "");
            temp.setAutoCommit(false);
            temp.setReadOnly(true);
            connections.add(temp);
            return temp;
        } catch(Exception ex) {
            Utils.close(temp);
            throw new RuntimeException("Cannot open dictionary " + name, ex);
        }
    }

    public void registerStatement(PreparedStatement ps) {
        statements.add(ps);
    }

    public String getUserKey(String name) {
        name = Utils.lower(name);
        MskKey key = context.getKeys().get(name);
        if (key==null)
            throw new IllegalArgumentException("Unknown masking key: " + name);
        return key.getValue();
    }

    public String injectKeys(String config) {
        while (true) {
            Matcher m = Pattern.compile("[{]([a-zA-Z0-9_\\-]+)[}]").matcher(config);
            if (! m.find())
                return config;
            final String keyName = m.group(1);
            final MskKey key = context.getKeys().get(keyName);
            if (key==null)
                throw new IllegalArgumentException("Unknown key name: " + keyName);
            config = config.replace("{" + keyName + "}", key.getValue());
        }
    }

    /**
     * Provide an executor for a predicate over a masking step.
     * @param step Masking step with a predicate.
     * @return Predicate executor, or null if step does not need a predicate.
     */
    public XExecutor makePredicate(MskStep step) {
        if (step==null || step.hasPredicate() == false)
            return null;
        XExecutor xe = predicates.get(step.getId());
        if (xe!=null)
            return xe;
        xe = new AlPredicateGroovy(services.getGroovyRunner(), step);
        predicates.put(step.getId(), xe);
        return xe;
    }

    /**
     * Provide an executor for a masking function.
     * The same function always gets the same executor
     * (e.g. executors are shared between the masking rules
     * in exactly the same way as masking functions).
     * @param func Masking function
     * @return Executor object for the masking function
     */
    public XExecutor makeExecutor(MskFunc func) {
        if (func==null)
            throw new IllegalArgumentException();
        XExecutor retval = executors.get(func.getId());
        if (retval != null)
            return retval;
        switch (func.getFunctionType()) {
            case Project:
                retval = new AlDefaultBatch(new AlProject(func));
                break;
            case Concat:
                retval = new AlDefaultBatch(new AlConcat(func));
                break;
            case Split:
                retval = new AlDefaultBatch(new AlSplit(func));
                break;
            case DateOp:
                retval = new AlDefaultBatch(new AlDateOp(this, func));
                break;
            case StringOp:
                retval = new AlDefaultBatch(new AlStringOp(func));
                break;
            case NumberHash:
                retval = new AlDefaultBatch(new AlNumberHash(this, func));
                break;
            case DigestHash:
                retval = new AlDefaultBatch(new AlDigestHash(this, func));
                break;
            case HashLookup:
                retval = new AlDefaultBatch(new AlHashLookup(this, func));
                break;
            case KeyLookup:
                retval = new AlDefaultBatch(new AlKeyLookup(this, func));
                break;
            case FPE:
                retval = new AlDefaultBatch(new AlFPE(this, func));
                break;
            case CharSubst:
                retval = new AlDefaultBatch(new AlCharSubst(this, func));
                break;
            case GroovyScript:
                retval = new AlDefaultBatch(
                        new AlGroovyScript(services.getGroovyRunner(), func));
                break;
            case ODPP:
                // The main reason to batch records - ODPP is JNI-based
                retval = new AlOptim(this, func);
                break;
            default:
                throw new IllegalArgumentException("Unsupported function type: "
                        + func.getFunctionType());
        }
        executors.put(func.getId(), retval);
        return retval;
    }

    @Override
    public void close() {
        for (PreparedStatement ps : statements)
            Utils.close(ps);
        statements.clear();
        for (Connection con : connections)
            Utils.close(con);
        connections.clear();
    }

    /**
     * Dump the performance statistics for all the executors which support it.
     * @param sb
     */
    public void dumpStats(StringBuilder sb) {
        for (XExecutor executor : executors.values()) {
            if (executor instanceof StatsDumper)
                ((StatsDumper)executor).dumpStats(sb);
        }
    }

}
