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
package com.ibm.dsmask.jconf.portage;

import com.ibm.dsmask.util.DsMaskUtil;
import groovy.lang.GroovyCodeSource;
import groovy.lang.GroovyShell;
import groovy.lang.Script;

/**
 *
 * @author zinal
 */
public class GroovyTester extends ScriptTesterBase {

    private static final org.slf4j.Logger LOG
            = org.slf4j.LoggerFactory.getLogger(GroovyTester.class);

    private GroovyShell groovyShell = null;
    private final String EOL = System.lineSeparator();

    public final Script compileInput(String text) {
        if (text.trim().startsWith("return")) {
            text = "def invoke(Object... input) {" + EOL + text + EOL + "}";
        }
        GroovyCodeSource gcs = new GroovyCodeSource(text, "script.groovy",
                GroovyShell.DEFAULT_CODE_BASE);
        return getShell().parse(gcs);
    }

    public final GroovyShell getShell() {
        if (groovyShell==null) {
            groovyShell = new GroovyShell();
            groovyShell.setVariable("dsmask", this);
        }
        return groovyShell;
    }

    @Override
    public boolean test(String text, String input, boolean predicate) {
        if (text==null || text.trim().length()==0)
            return false;

        final Script script;
        try {
            getShell();
            script = compileInput(text);
        } catch(Throwable ex) {
            LOG.error("Compilation failed for Groovy function\n{}\n", text, ex);
            return false;
        }

        if (input==null || input.trim().length()==0) {
            // No input to run the function, skipping execution
            return true;
        }

        final Script inputGenerator;
        try {
            inputGenerator = compileInput(input);
        } catch(Throwable ex) {
            LOG.error("Compilation failed for Groovy function "
                    + "input generator\n{}\n", input, ex);
            return false;
        }

        Object inputData;
        try {
            inputData = inputGenerator.invokeMethod("invoke", new Object[]{});
        } catch(Throwable ex) {
            LOG.error("Execution failed for Groovy function "
                    + "input generator\n{}\n", input, ex);
            return false;
        }

        if (inputData==null) {
            inputData = new Object[] {};
        }
        LOG.debug("{}", inputData);

        Object results = null;
        try {
            results = script.invokeMethod("invoke", inputData);
        } catch(Throwable ex) {
            LOG.error("Execution failed for Groovy function\n{}\n", text, ex);
            return false;
        }

        if (LOG.isDebugEnabled()) {
            if (results==null)
                results = "RESULTS WERE VOID";
            LOG.debug("{}", results);
        }

        if (predicate) {
            Boolean retval = null;
            if (results == null) {
                retval = Boolean.FALSE;
            } else if (results instanceof Boolean) {
                retval = (Boolean)results;
            } else {
                retval = DsMaskUtil.toBoolean(results.toString());
            }
            if (retval.booleanValue())
                return true;
            LOG.error("Negative result for the predicate\n\t{}\n"
                    + "on input\n\t{}\n", text, input);
            return false;
        }

        return true;
    }

}
