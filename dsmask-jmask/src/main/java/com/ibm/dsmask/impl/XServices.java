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

import com.ibm.nex.odpp.ODPP;
import com.ibm.dsmask.uniq.UniqProvider;
import com.ibm.dsmask.uniq.UniqProviderFactory;
import com.ibm.dsmask.SafeLogger;
import com.ibm.dsmask.ops.AlgoInitException;

/**
 *
 * @author zinal
 */
public class XServices implements AutoCloseable {

    private GroovyRunner groovyRunner = null;
    private ODPP odpp = null;

    private UniqProviderFactory uniqProviderFactory = null;
    private UniqProvider uniqProvider = null;

    public GroovyRunner getGroovyRunner() {
        if (groovyRunner==null)
            groovyRunner = new GroovyRunner();
        return groovyRunner;
    }

    public ODPP getODPP() {
        if (odpp==null) {
            odpp = new ODPP();
            odpp.setJNILIBNAME("ODPPJNI.11.3");
            odpp.initialize();
        }
        return odpp;
    }

    /**
     * Re-set the iteration number for the Lua and Groovy scripts to zero.
     */
    public void resetIterationNumber() {
        if (groovyRunner != null)
            groovyRunner.setCurrentIteration(0);
    }

    public UniqProviderFactory getUniqProviderFactory() {
        return uniqProviderFactory;
    }

    public void setUniqProviderFactory(UniqProviderFactory f) {
        this.uniqProviderFactory = f;
    }

    /**
     * Initialize and return the uniq-check provider, if one is configured.
     * Typically returns the instance of UniqClient class.
     * @return UniqProvider instance, or throws an exception
     */
    public UniqProvider getUniqProvider() {
        if (uniqProvider==null) {
            UniqProviderFactory factory = getUniqProviderFactory();
            if (factory==null)
                throw new AlgoInitException("uniq-check is not configured");
            uniqProvider = factory.createProvider();
        }
        return uniqProvider;
    }

    @Override
    public void close() {
        if (odpp!=null) {
            try {
                odpp.shutdown();
            } catch(Exception ex) {
                SafeLogger.warning("ODPP shutdown failed", ex);
            }
            odpp = null;
        }
        if ( uniqProvider != null ) {
            uniqProvider.close();
            uniqProvider = null;
        }
    }

}
