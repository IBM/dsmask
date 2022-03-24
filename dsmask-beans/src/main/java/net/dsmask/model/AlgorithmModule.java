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

import java.util.HashMap;
import java.util.Map;

/**
 * Module of masking algorithms.
 * @author zinal
 */
public class AlgorithmModule {

    private final String moduleName;
    private final Map<String, AlgorithmInfo> algorithms = new HashMap<>();

    public AlgorithmModule(String moduleName) {
        this.moduleName = moduleName;
    }

    public String getModuleName() {
        return moduleName;
    }

    public void registerAlgorithm(AlgorithmInfo ai) {
        String algoName = AlgorithmInfo.safeId(ai.getName(), moduleName);
        algorithms.put(algoName, ai);
    }

    public AlgorithmInfo findAlgorithm(String algoName) {
        algoName = AlgorithmInfo.safeId(algoName, moduleName);
        return algorithms.get(ModelUtils.lower(algoName));
    }

}
