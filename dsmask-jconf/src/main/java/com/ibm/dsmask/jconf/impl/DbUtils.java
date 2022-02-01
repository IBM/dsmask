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

import java.io.File;
import java.io.FileFilter;
import java.sql.Connection;
import java.sql.Statement;

/**
 *
 * @author zinal
 */
public class DbUtils {

    public static String resolvePath(String path) {
        if (path.startsWith("~/"))
            return System.getProperty("user.home") + path.substring(1);
        return path;
    }

    public static void deleteFiles(String pathname) {
        pathname = resolvePath(pathname);
        final File pn = new File(pathname).getAbsoluteFile();
        final String fullName = pn.getAbsolutePath();
        final String fullStart = fullName + ".";
        File[] victims = pn.getParentFile().listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                final String cur = pathname.getAbsolutePath();
                return (fullName.equals(cur)
                        || cur.startsWith(fullStart));
            }
        });
        if (victims!=null) {
            for (File v : victims)
                v.delete();
        }
    }

    public static String makeConnectionUrl(String pathname) {
        return "jdbc:h2:" + resolvePath(pathname).replaceAll("\\\\", "/");
    }

    public static void createTables(Connection con, String[] cmds)
            throws Exception {
        try (Statement stmt = con.createStatement()) {
            for (String sql : cmds)
                stmt.execute(sql);
        }
    }

}
