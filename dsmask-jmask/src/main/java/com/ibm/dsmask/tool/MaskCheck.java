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
package com.ibm.dsmask.tool;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import com.ibm.dsmask.beans.*;
import com.ibm.dsmask.impl.*;

/**
 * Masking rules standalone executor/debugger (prototype).
 * @author zinal
 */
public class MaskCheck {

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("USAGE: "
                    + MaskCheck.class.getName()
                    + " jobfile.xml");
            System.exit(1);
        }
        try {
            final Properties props = new Properties();
            try (FileInputStream fis = new FileInputStream(args[0])) {
                props.loadFromXML(fis);
            }
            new MaskCheck(props).run();
        } catch(Exception ex) {
            ex.printStackTrace(System.err);
            System.exit(1);
        }
    }

    private String dictPath;
    private String configPath;
    private String configName;
    private String databaseName;
    private String tableName;
    private List<String> scopeList;

    private String inputScript;

    private MaskCheck(Properties props) {
        dictPath = props.getProperty("dsmask.dict.path", "dict");
        configPath = props.getProperty("dsmask.config.path", "config");
        configName = props.getProperty("dsmask.config.name", "default");
        databaseName = props.getProperty("dsmask.db.name", "db1");
        tableName = props.getProperty("dsmask.table.name", "schema1.table1");
        int scopeNum = 0;
        while (true) {
            String scopeName = "dsmask.scope.name." + String.valueOf(++scopeNum);
            String scopeVal = props.getProperty(scopeName);
            if (scopeVal!=null && scopeVal.length() > 0) {
                if (scopeList == null)
                    scopeList = new ArrayList<>();
                scopeList.add(scopeVal);
            } else {
                // no more scope values
                break;
            }
        }
        inputScript = props.getProperty("dsmask.input.script", "dsmask-input.groovy");
    }

    private void run() throws Exception {
        final MskContext context;
        try (ContextLoader ctxLoader
                = new ContextLoader(configPath, configName)) {
            context = ctxLoader.load(databaseName, tableName, scopeList);
        } catch(Exception ex) {
            throw new Exception("Failed to load configuration database ["
                    + configName + "]", ex);
        }
        if (context == null) {
            System.out.println("NOTE: Nothing to mask - no context loaded");
        } else {
            context.setDictPath(dictPath);
        }

        GroovyInputLink input = new GroovyInputLink(new GroovyRunner(),
                inputScript);
        TraceOutputLink output = new TraceOutputLink(input);

        XMasker masker = new XMasker(input, output, 1);
        masker.setContext(context);
        masker.buildIndexMap();
        masker.validateFields();
        masker.run();
    }

}
