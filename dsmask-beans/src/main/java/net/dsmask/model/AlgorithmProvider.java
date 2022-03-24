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
package net.dsmask.model;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import net.dsmask.model.xml.AlgoSerializer;

/**
 * Masking algorithm provider, holding a collection of the defined
 * algorithm modules.
 * @author zinal
 */
public class AlgorithmProvider {

    private final Map<String, AlgorithmModule> modules = new HashMap<>();

    private static final AlgorithmProvider INSTANCE = new AlgorithmProvider();

    private AlgorithmProvider() {
        modules.put(AlgorithmInfo.MODULE_DEFAULT, loadDefaultModule());
    }

    public static AlgorithmProvider getInstance() {
        return INSTANCE;
    }

    public void registerModule(AlgorithmModule am) {
        String moduleName = ModelUtils.lower(am.getModuleName());
        if (moduleName.length() == 0)
            moduleName = AlgorithmInfo.MODULE_DEFAULT;
        synchronized(modules) {
            if (modules.get(moduleName) == null)
                modules.put(moduleName, am);
        }
    }

    public AlgorithmInfo findAlgorithm(String name) {
        String moduleName = AlgorithmInfo.moduleId(name);
        AlgorithmModule am;
        synchronized(modules) {
            am = modules.get(moduleName);
        }
        return (am==null) ? null : am.findAlgorithm(name);
    }

    public static AlgorithmModule loadDefaultModule() {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        try (InputStream stream = loader
                .getResourceAsStream("dsmask-algo-builtin.xml")) {
            return AlgoSerializer.readStream(stream);
        } catch(Exception ex) {
            throw new RuntimeException("Failed to load the "
                    + "definition of the default algorithm module", ex);
        }
    }
}
