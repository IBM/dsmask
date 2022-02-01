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
import com.ibm.dsmask.beans.MskFunc;

/**
 * Execute Groovy scripts as data masking transformations.
 * @author zinal
 */
public class AlGroovyScript implements AlSimpleVector {

    private final GroovyRunner groovyRunner;
    private final boolean iterable;
    private final Script script;

    public AlGroovyScript(GroovyRunner groovyRunner, MskFunc function) {
        this.groovyRunner = groovyRunner;
        this.iterable = function.isIterable();
        this.script = groovyRunner.compileInput(function.getText());
    }

    @Override
    public XVector exec(XVector in, XVector out, int iteration) {
        // Set the iteration number
        groovyRunner.setCurrentIteration(iteration);
        // Invoke script
        final Object retval;
        // invoking the script with the current arguments
        retval = script.invokeMethod(GroovyRunner.INVOKE_NAME, in.values);
        // pack the output according to its structure
        if (retval==null) {
            // null is null
            out = XVector.make(out, 1);
            out.values[0] = null;
        } else if (retval.getClass().isArray()
                && ! retval.getClass().getComponentType().isPrimitive()
                ) {
            // array of objects come as multiple output values
            Object[] data = (Object[]) retval;
            out = XVector.make(out, data.length);
            for (int i=0; i<data.length; ++i) {
                out.values[i] = data[i];
            }
        } else {
            // array of primitive values come as a single packed value
            // (typically byte[])
            out = XVector.make(out, 1);
            out.values[0] = retval;
        }
        return out;
    }

    @Override
    public XVector exec(XVector in, XVector out) {
        return exec(in, out, 0);
    }

    @Override
    public boolean isIterationsSupported() {
        return iterable;
    }

}
