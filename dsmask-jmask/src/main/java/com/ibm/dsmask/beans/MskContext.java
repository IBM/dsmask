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
package com.ibm.dsmask.beans;

import java.util.HashMap;
import java.util.Map;

/**
 * Data masking context: table profile plus all related stuff.
 * @author zinal
 */
public class MskContext {

    private String dictPath = null;

    private final MskTable table = new MskTable();
    private final Map<Integer, MskFunc> functions = new HashMap<>();
    private final Map<Integer, MskRule> rules = new HashMap<>();
    private final Map<String, MskKey> keys = new HashMap<>();

    public String getDictPath() {
        return dictPath;
    }

    public void setDictPath(String dictPath) {
        this.dictPath = dictPath;
    }

    public MskTable getTable() {
        return table;
    }

    public Map<Integer, MskFunc> getFunctions() {
        return functions;
    }

    public MskFunc getFunction(int id) {
        return functions.get(id);
    }

    public void addFunction(MskFunc f) {
        functions.put(f.getId(), f);
    }

    public Map<Integer, MskRule> getRules() {
        return rules;
    }

    public MskRule getRule(int id) {
        return rules.get(id);
    }

    public void addRule(MskRule r) {
        rules.put(r.getId(), r);
    }

    public final String getTableInfo() {
        return table.getTableInfo();
    }

    public Map<String, MskKey> getKeys() {
        return keys;
    }

    public void addKey(MskKey key) {
        keys.put(key.getName(), key);
    }

}
