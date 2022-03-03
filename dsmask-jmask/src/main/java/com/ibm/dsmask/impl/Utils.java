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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.text.StringTokenizer;
import net.dsmask.util.DsMaskUtil;

/**
 * Global utilities.
 * @author zinal
 */
public class Utils extends DsMaskUtil {

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
        if (ps==null)
            return;
        try {
            ps.close();
        } catch(Exception ex) {}
    }

    public static void close(Connection con) {
        if (con==null)
            return;
        try {
            if (!con.isReadOnly())
                con.rollback();
        } catch(Exception ex) {}
        try {
            con.close();
        } catch(Exception ex) {}
    }

    public static String unquote(String s) {
        if (s.startsWith("\"") && s.endsWith("\"") && s.length()>1) {
            s = s.substring(1, s.length()-1);
        }
        return s.trim();
    }

    /**
     * Parse a text block representing the configuration into the list
     * of string arrays. Each array contains the tokens in the corresponding
     * configuration line.
     * @param text Input text block
     * @return Parsed text block
     */
    public static List<String[]> parseConfig(String text) {
        String[] lines = text.split("[\n\r]+");
        final ArrayList<String[]> retval = new ArrayList<>();
        for (String line : lines) {
            final String[] tokens = new StringTokenizer(line).getTokenArray();
            for (int i=0; i<tokens.length; ++i)
                tokens[i] = unquote(tokens[i]);
            if (tokens.length > 0)
                retval.add(tokens);
        }
        return retval;
    }

    /**
     * Check whether the specified configuration entry exists in
     * a list of key-value pairs.
     * @param lines Input key-value pairs
     * @param key The key to search for
     * @return true, if the key exists, false otherwise
     */
    public static boolean hasConfigEntry(List<String[]> lines, String key) {
        for (String[] line : lines) {
            if (line.length<2)
                continue;
            if (key.equalsIgnoreCase(line[0]))
                return true;
        }
        return false;
    }

    /**
     * Retrieve the configuration value from a list of key-value pairs
     * @param lines Input key-value pairs
     * @param key The key to search for
     * @return The value. Exception is thrown if no value was found.
     */
    public static String getConfigValue(List<String[]> lines, String key) {
        for (String[] line : lines) {
            if (line.length<2)
                continue;
            if (key.equalsIgnoreCase(line[0])) {
                if (line.length==2)
                    return line[1];
                // Reconstruct the full line from the items, using TAB separator
                final StringBuilder buf = new StringBuilder();
                for (int pos=1; pos<line.length; pos++) {
                    if (pos>1)
                        buf.append("\t");
                    buf.append(line[pos]);
                }
                return buf.toString();
            }
        }
        throw new IllegalArgumentException("Missing required key " + key
            + ", got input " + lines);
    }

    public static String[] getConfigArgs(List<String[]> lines, String key) {
        for (String[] line : lines) {
            if (line.length<2)
                continue;
            if (key.equalsIgnoreCase(line[0])) {
                return Arrays.copyOfRange(line, 1, line.length);
            }
        }
        throw new IllegalArgumentException("Missing required key " + key
            + ", got input " + lines);
    }

    public static int getConfigInt(List<String[]> lines, String key, int defval) {
        if (!hasConfigEntry(lines, key))
            return defval;
        String val = getConfigValue(lines, key);
        try {
            return Integer.parseInt(val);
        } catch(NumberFormatException nfe) {
            throw new IllegalArgumentException("Incorrect value for key " + key
                + ", expected integer, got input " + val);
        }
    }

    public static long getConfigLong(List<String[]> lines, String key, long defval) {
        if (!hasConfigEntry(lines, key))
            return defval;
        String val = getConfigValue(lines, key);
        try {
            return Long.parseLong(val);
        } catch(NumberFormatException nfe) {
            throw new IllegalArgumentException("Incorrect value for key " + key
                + ", expected integer, got input " + val);
        }
    }

    public static boolean getConfigBool(List<String[]> lines, String key, boolean defval) {
        if (!hasConfigEntry(lines, key))
            return defval;
        final String v = getConfigValue(lines, key).trim();
        if (v.length() == 0)
            return defval;
        return toBoolean(v);
    }

    /**
     * Convert the string containing a comma-separated list of integers
     * to the array of actual integer values.
     * @param v Input string
     * @return The result of parsing
     */
    public static int[] parseIndexes(String v) {
        String[] items = v.split("[,][ \t]*");
        final ArrayList<Integer> data = new ArrayList<>();
        for (String x : items) {
            if (x.contains("-")) {
                String[] pair = v.split("[\\-]");
                int start = Integer.valueOf(pair[0].trim());
                int finish = Integer.valueOf(pair[1].trim());
                if (start>finish || (finish-start)>=1000)
                    throw new IllegalArgumentException("Bad indexes: [" + v + "]");
                for (int i=start; i<=finish; ++i)
                    data.add(i);
            } else {
                data.add(Integer.valueOf(x.trim()));
            }
        }
        int[] retval = new int[data.size()];
        for (int i=0; i<data.size(); ++i)
            retval[i] = data.get(i);
        return retval;
    }

}
