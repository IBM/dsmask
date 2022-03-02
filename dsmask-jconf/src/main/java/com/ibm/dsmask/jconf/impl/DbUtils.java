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

import java.sql.Connection;
import java.sql.Statement;
import net.dsmask.util.DsMaskUtil;

/**
 *
 * @author zinal
 */
public class DbUtils extends DsMaskUtil {

    public static void createTables(Connection con, String[] cmds)
            throws Exception {
        try (Statement stmt = con.createStatement()) {
            for (String sql : cmds)
                stmt.execute(sql);
        }
    }

}
