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
package net.dsmask.util;

import java.io.File;
import java.io.FileFilter;

/**
 * Miscellaneous utility methods which are hard to put elsewhere.
 * @author zinal
 */
public class DsMaskUtil {

    /**
     * Convert the code point to a string representation
     * @param sb String builder to append the code point to, or null to allocate a new one
     * @param cp Code point to be converted
     * @return Same string builder object as passed on input, or a new one if null was passed
     */
    public static StringBuilder appendCodepoint(StringBuilder sb, int cp) {
        if (sb==null)
            sb = new StringBuilder();
        if (Character.isBmpCodePoint(cp)) {
            sb.append((char) cp);
        } else if (Character.isValidCodePoint(cp)) {
            sb.append(Character.highSurrogate(cp));
            sb.append(Character.lowSurrogate(cp));
        } else {
            sb.append('?');
        }
        return sb;
    }

    /**
     * Convert the code point to a string representation
     * @param cp Code point to be converted
     * @return String representation of the code point on input
     */
    public static String fromCodepoint(int cp) {
        return appendCodepoint(null, cp).toString();
    }

    /**
     * Convert the input value to boolean
     * @param val input value
     * @return true or false (default false)
     */
    public static boolean toBoolean(String val) {
        if (val==null || val.length()==0)
            return false;
        char c = val.charAt(0);
        return ('1'==c)
                || ('Y'==c) || ('y'==c)
                || ('T'==c) || ('t'==c);
    }

    /**
     * Resolve pathname potentially starting with "~/".
     * @param path Input path
     * @return Resolved path
     */
    public static String resolvePath(String path) {
        if (path.startsWith("~/"))
            return System.getProperty("user.home") + path.substring(1);
        return path;
    }

    /**
     * Delete H2 database files denoted by some pathname
     * @param pathname H2 database pathname
     */
    public static void deleteFiles(String pathname) {
        pathname = resolvePath(pathname);
        deleteFiles(new File(pathname));
    }

    /**
     * Delete H2 database files denoted by some pathname
     * @param pathname H2 database pathname
     */
    public static void deleteFiles(File pathname) {
        final File pn = pathname.getAbsoluteFile();
        final String fullName = pn.getAbsolutePath();
        final String fullStart = fullName + ".";
        File[] victims = pn.getParentFile().listFiles(new FileFilter() {
            @Override
            public boolean accept(File x) {
                final String cur = x.getAbsolutePath();
                return (fullName.equals(cur)
                        || cur.startsWith(fullStart));
            }
        });
        if (victims!=null) {
            for (File v : victims)
                v.delete();
        }
    }

    /**
     * Generate H2 database URL based on pathname
     * @param pathname H2 database pathname
     * @return H2 database URL
     */
    public static String makeConnectionUrl(String pathname) {
        return "jdbc:h2:" + resolvePath(pathname).replaceAll("\\\\", "/");
    }

    public static RuntimeException toRE(String message, Throwable ex) {
        String msg = message + ": "
                + ( (ex.getMessage() == null) ?
                    ex.getClass().getSimpleName() :
                    ex.getMessage() );
        return new RuntimeException(msg, ex);
    }

}
