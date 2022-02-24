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
package com.ibm.dsmask.jconf.beans;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

/**
 * Global utilities.
 * @author zinal
 */
public class Utils {

    public static final String NONE = "";

    public static org.slf4j.Logger logger(Class<?> clazz) {
        return org.slf4j.LoggerFactory.getLogger(clazz);
    }

    public static String lower(String s) {
        return (s==null) ? "" : s.trim().toLowerCase();
    }

    public static <T> T getLast(final List<T> list) {
        if (list==null || list.isEmpty())
            return null;
        return list.get(list.size()-1);
    }

    public static void close(ResultSet rs) {
        try {
            if (rs!=null)
                rs.close();
        } catch(Exception ex) {}
    }

    public static void close(PreparedStatement ps) {
        try {
            if (ps!=null)
                ps.close();
        } catch(Exception ex) {}
    }

    public static void close(Connection con) {
        if (con==null)
            return;
        try {
            if (! con.getAutoCommit())
                con.rollback();
        } catch(Exception ex) {}
        try {
            con.close();
        } catch(Exception ex) {}
    }

    public static String safeConfigName(String name) {
        name = lower(name);
        name = name.replaceAll("[.:*/\\\\]", "-");
        return name;
    }

    public static String makeConfigDbPath(String configDir, String configName) {
        return new File(new File(configDir), configName)
                .getAbsolutePath().replaceAll("\\\\", "/");
    }

    public static String unquote(String s) {
        if (s.startsWith("\"") && s.endsWith("\"") && s.length()>1) {
            s = s.substring(1, s.length()-1);
        }
        return s.trim();
    }
}
