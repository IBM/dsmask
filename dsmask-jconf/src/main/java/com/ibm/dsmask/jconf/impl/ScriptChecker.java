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
import com.ibm.dsmask.jconf.beans.*;
import com.ibm.dsmask.jconf.portage.*;

/**
 * Validate syntax of scripts and perform basic execution tests.
 * @author zinal
 */
public class ScriptChecker {

    private static final org.slf4j.Logger LOG = Utils.logger(ScriptChecker.class);

    private GroovyTester groovyTester = null;

    private final MaskingRuleRegistry ruleReg;

    public ScriptChecker(MaskingRuleRegistry ruleReg) {
        this.ruleReg = ruleReg;
    }

    private GroovyTester getGroovyTester() {
        if (groovyTester==null)
            groovyTester = new GroovyTester();
        return groovyTester;
    }

    public void check() throws Exception {
        int functionsTested = 0, functionsFailed = 0;
        for (MaskingFunction mf : ruleReg.getFunctions().values()) {
            switch (mf.getType()) {
                case GroovyScript:
                    if (!getGroovyTester().test(mf))
                        ++functionsFailed;
                    ++functionsTested;
                    break;
                default: /*noop*/
                    break;
            }
        }
        int predicatesTested = 0, predicatesFailed = 0;
        for (List<MaskingRule> mrs : ruleReg.getRules().values()) {
            for (MaskingRule mr : mrs) {
                for (MaskingStep ms : mr.getPipeline()) {
                    if (ms.getPredicate()==null)
                        continue;
                    if (!getGroovyTester().test(ms.getPredicate()))
                        ++predicatesFailed;
                    ++predicatesTested;
                }
            }
        }
        LOG.info("Tested {} script functions, with total {} failures.",
                functionsTested, functionsFailed);
        LOG.info("Tested {} script predicates, with total {} failures.",
                predicatesTested, predicatesFailed);
        if (functionsFailed>0)
            throw new Exception("Incorrect script functions found");
    }

}
