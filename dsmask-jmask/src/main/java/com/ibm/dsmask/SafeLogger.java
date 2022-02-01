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
package com.ibm.dsmask;

import com.ibm.is.cc.javastage.api.Logger;

/**
 * Logger which works both for DataStage and mock environments.
 * @author mzinal
 */
public class SafeLogger {

    private final static Object guard = new Object();
    private static LogI log = null;

    private static LogI getLog() {
        synchronized(guard) {
            if (log==null) {
                try {
                    Logger.isDebugEnabled();
                    log = new LogDS();
                } catch(Throwable ex) {
                    log = new LogMock();
                }
            }
            return log;
        }
    }

    public static StringBuilder formatException(StringBuilder sb,
            String message, Throwable err) {
        final String eol = System.getProperty("line.separator");
        if (message!=null && message.length() > 0)
            sb.append(message);
        else
            sb.append("EXCEPTION");
        while (err != null) {
            sb.append(eol).append("\t");
            sb.append(err.getClass().getSimpleName());
            if (err.getMessage()!=null)
                sb.append(": ").append(err.getMessage());
        }
        return sb;
    }
    
    public static StringBuilder formatException(String message, Throwable err) {
        return formatException(new StringBuilder(), message, err);
    }

    public static void fatal(String message, Throwable err) {
        getLog().fatal(formatException(message, err).toString());
    }

    public static void fatal(String message) {
        getLog().fatal(message);
    }

    public static void warning(String message) {
        getLog().warning(message);
    }

    public static void warning(String message, Throwable err) {
        getLog().warning(formatException(message, err).toString());
    }

    public static void information(String message) {
        getLog().information(message);
    }

    public static void information(String message, Throwable err) {
        getLog().information(formatException(message, err).toString());
    }

    public static void debug(String message) {
        getLog().debug(message);
    }

    public static void debug(String message, Throwable err) {
        if (isDebugEnabled()) {
            getLog().debug(formatException(message, err).toString());
        }
    }

    public static boolean isDebugEnabled() {
        return getLog().isDebugEnabled();
    }

    static interface LogI {
        void fatal(String message);
        void warning(String message);
        void information(String message);
        void debug(String message);
        boolean isDebugEnabled();
    }

    static final class LogMock implements LogI {

        @Override
        public void fatal(String message) {
            System.out.println("log-mock FATA: " + message);
        }

        @Override
        public void warning(String message) {
            System.out.println("log-mock WARN: " + message);
        }

        @Override
        public void information(String message) {
            System.out.println("log-mock INFO: " + message);
        }

        @Override
        public void debug(String message) {
            System.out.println("log-mock DEBU: " + message);
        }

        @Override
        public boolean isDebugEnabled() {
            return true;
        }

    }

    static final class LogDS implements LogI {

        @Override
        public void fatal(String message) {
            Logger.fatal(message);
        }

        @Override
        public void warning(String message) {
            Logger.warning(message);
        }

        @Override
        public void information(String message) {
            Logger.information(message);
        }

        @Override
        public void debug(String message) {
            Logger.debug(message);
        }

        @Override
        public boolean isDebugEnabled() {
            return Logger.isDebugEnabled();
        }

    }

}
