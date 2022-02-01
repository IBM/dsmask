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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.ibm.dsmask.jconf.beans.*;

/**
 *
 * @author zinal
 */
public class MaskingRuleRegistry {

    private final Map<String, MaskingKey> keys = new HashMap<>();
    private final Map<String, MaskingFunction> functions = new HashMap<>();
    private final Map<String, List<MaskingRule>> rules = new HashMap<>();

    private static final String EMPTY = "";

    public MaskingRuleRegistry() {
    }

    public MaskingRuleRegistry(
            Collection<MaskingKey> keys,
            Collection<MaskingFunction> functions,
            Collection<MaskingRule> rules) {
        this.populate(keys, functions, rules);
    }

    public void populate() {
        // noop here, can be overridden
    }

    public int getKeyCount() {
        return keys.size();
    }

    public int getFunctionCount() {
        return functions.size();
    }

    public int getRulesCount() {
        int count = 0;
        for (List<MaskingRule> mrs : rules.values()) {
            count += mrs.size();
        }
        return count;
    }

    protected final void populate(
            Collection<MaskingKey> keys,
            Collection<MaskingFunction> functions,
            Collection<MaskingRule> rules) {
        for (MaskingKey key : keys) {
            this.keys.put(key.getName(), key);
        }
        for (MaskingFunction fun : functions) {
            if (!fun.isValid()) {
                throw new IllegalArgumentException("Invalid function ["
                        + fun.getName() + "] - cannot populate");
            }
            this.functions.put(fun.getName(), fun);
        }
        for (MaskingRule rule : rules) {
            if (!rule.isValid()) {
                throw new IllegalArgumentException("Invalid rule ["
                        + rule.getName() + "] - cannot populate");
            }
            for (MaskingStep ms : rule.getPipeline()) {
                if (ms.getFunction() !=
                        this.functions.get(ms.getFunction().getName())) {
                    throw new IllegalArgumentException("Rule ["
                        + rule.getName() + "] refers to unknown function ["
                        + ms.getFunction().getName() + "] - cannot populate");
                }
            }
            Set<String> contexts = rule.getContexts();
            if (contexts==null || contexts.isEmpty())
                contexts = Collections.singleton(EMPTY);
            for (String ctx : contexts) {
                List<MaskingRule> cur = this.rules.get(ctx);
                if (cur==null) {
                    cur = new ArrayList<>();
                    this.rules.put(ctx, cur);
                }
                cur.add(rule);
            }
        }
    }

    public final Map<String, MaskingKey> getKeys() {
        return keys;
    }

    public final Map<String, MaskingFunction> getFunctions() {
        return functions;
    }

    public final Map<String, List<MaskingRule>> getRules() {
        return rules;
    }

    public MaskingKey findKey(String name) {
        return keys.get(Utils.lower(name));
    }

    public MaskingFunction findFunction(String name) {
        return functions.get(Utils.lower(name));
    }

    public MaskingRule findRule(String name) {
        name = Utils.lower(name);
        for (List<MaskingRule> mrs : rules.values()) {
            for (MaskingRule mr : mrs)
                if (name.equals(mr.getName()))
                    return mr;
        }
        throw new IllegalArgumentException("Unknown masking rule [" + name + "]");
    }

    public List<MaskingRule> retrieveRules(String context) {
        List<MaskingRule> cur = rules.get(Utils.lower(context));
        if (cur==null)
            return Collections.emptyList();
        return Collections.unmodifiableList(cur);
    }

    public List<MaskingKey> retrieveKeys() {
        return new ArrayList<>(keys.values());
    }

}
