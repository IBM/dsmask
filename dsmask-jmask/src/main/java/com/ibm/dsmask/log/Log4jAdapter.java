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
package com.ibm.dsmask.log;

import com.ibm.dsmask.SafeLogger;
import org.slf4j.helpers.MarkerIgnoringBase;

/**
 * Work in progress:
 * Adapter for Log4j API to log messages to DataStage.
 * @author zinal
 */
public class Log4jAdapter extends MarkerIgnoringBase {

    @Override
    public boolean isTraceEnabled() {
        return false;
    }

    @Override
    public void trace(String msg) {
        /* noop */
    }

    @Override
    public void trace(String format, Object arg) {
        /* noop */
    }

    @Override
    public void trace(String format, Object arg1, Object arg2) {
        /* noop */
    }

    @Override
    public void trace(String format, Object... arguments) {
        /* noop */
    }

    @Override
    public void trace(String msg, Throwable t) {
        /* noop */
    }

    @Override
    public boolean isDebugEnabled() {
        return SafeLogger.isDebugEnabled();
    }

    @Override
    public void debug(String msg) {
        SafeLogger.debug(msg);
    }

    @Override
    public void debug(String format, Object arg) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void debug(String format, Object arg1, Object arg2) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void debug(String format, Object... arguments) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void debug(String msg, Throwable t) {
        SafeLogger.debug(msg, t);
    }

    @Override
    public boolean isInfoEnabled() {
        return true;
    }

    @Override
    public void info(String msg) {
        SafeLogger.information(msg);
    }

    @Override
    public void info(String format, Object arg) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void info(String format, Object arg1, Object arg2) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void info(String format, Object... arguments) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void info(String msg, Throwable t) {
        SafeLogger.information(msg, t);
    }

    @Override
    public boolean isWarnEnabled() {
        return true;
    }

    @Override
    public void warn(String msg) {
        SafeLogger.warning(msg);
    }

    @Override
    public void warn(String format, Object arg) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void warn(String format, Object... arguments) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void warn(String format, Object arg1, Object arg2) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void warn(String msg, Throwable t) {
        SafeLogger.warning(msg, t);
    }

    @Override
    public boolean isErrorEnabled() {
        return true;
    }

    @Override
    public void error(String msg) {
        SafeLogger.fatal(msg);
    }

    @Override
    public void error(String format, Object arg) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void error(String format, Object arg1, Object arg2) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void error(String format, Object... arguments) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void error(String msg, Throwable t) {
        SafeLogger.fatal(msg, t);
    }
    
}
