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

import groovy.lang.GroovyCodeSource;
import groovy.lang.GroovyShell;
import groovy.lang.Script;
import java.io.File;

/**
 *
 * @author zinal
 */
public class GroovyRunner extends AbstractRunner {

    public static final String INVOKE_NAME = "invoke";
    
    private final GroovyShell groovyShell;
    private final String EOL;

    public GroovyRunner() {
        this.groovyShell = new GroovyShell();
        this.groovyShell.setVariable("dsmask", this);
        this.EOL = System.lineSeparator();
    }

    public Script compileInput(String text) {
        if (text.trim().startsWith("return")) {
            text = "def " + INVOKE_NAME + "(Object... input) {" + EOL + text + EOL + "}";
        }
        GroovyCodeSource gcs = new GroovyCodeSource(text, "script.groovy",
                GroovyShell.DEFAULT_CODE_BASE);
        return groovyShell.parse(gcs);
    }

    public Script compileFile(String fname) {
        try {
            GroovyCodeSource gcs = new GroovyCodeSource(new File(fname), "UTF-8");
            return groovyShell.parse(gcs);
        } catch(Exception ex) {
            throw new RuntimeException("Failed to parse " + fname, ex);
        }
    }

}
