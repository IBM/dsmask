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

import groovy.lang.Script;
import com.ibm.dsmask.impl.*;
import com.ibm.dsmask.beans.*;

/**
 * Execute a Groovy function as a predicate expression.
 *
 * @author zinal
 */
public class AlPredicateGroovy implements XExecutor {

    private final MskStep step;
    private final Script script;

    public AlPredicateGroovy(GroovyRunner groovyRunner, MskStep step) {
        if (!step.hasPredicate()) {
            throw new IllegalArgumentException(step.toString());
        }
        this.step = step;
        this.script = groovyRunner.compileInput(step.getPredicateText());
    }

    public MskStep getStep() {
        return step;
    }

    @Override
    public void exec(XWorkspace ws) {
        for (int i = 0; i < ws.totalRows; ++i) {
            if (ws.errors[i].hasError()) {
                ws.predicates[i] = false;
                continue; // skip rows with errors
            }
            final Object[] inputs = ws.inputs[i].values;
            // Invoke script
            final Object retval = script.invokeMethod(GroovyRunner.INVOKE_NAME, inputs);
            if (retval == null) {
                ws.predicates[i] = false;
            } else if (retval instanceof Boolean) {
                ws.predicates[i] = (Boolean) retval;
            } else {
                ws.predicates[i] = Utils.toBoolean(retval.toString());
            }
        }
    }

}
