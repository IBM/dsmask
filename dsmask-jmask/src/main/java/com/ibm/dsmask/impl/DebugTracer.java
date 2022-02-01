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
import java.io.FileOutputStream;
import java.io.PrintWriter;

/**
 * Helper for debug tracing, usable for problem diagnostics.
 * @author zinal
 */
public class DebugTracer {

    public static boolean isTraceEnabled() {
        return false;
    }

    public static void trace(String category, String message) {
        if (!isTraceEnabled())
            return;
        try {
            File f = new File(System.getProperty("java.io.tmpdir"),
                    "dsmask-" + category + ".txt");
            try (PrintWriter pw = new PrintWriter(new FileOutputStream(f, true))) {
                pw.println(message);
            }
        } catch(Exception ex) {}
    }

}
