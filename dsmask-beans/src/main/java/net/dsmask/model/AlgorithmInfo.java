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

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Objects;

/**
 * Data masking algorithm used to implement the data masking function.
 * @author zinal
 */
public class AlgorithmInfo implements ModelIdentity {

    public static String MODULE_DEFAULT = "dsmask";

    private final String name;
    private final boolean iterable;
    private final LinkedHashMap<String, AlgorithmParameter> parameters = new LinkedHashMap<>();

    public AlgorithmInfo(String name) {
        this(name, false);
    }

    public AlgorithmInfo(String name, boolean iterable) {
        this.name = ModelUtils.safe(name);
        this.iterable = iterable;
    }

    @Override
    public String getId() {
        return ModelUtils.lower(name);
    }

    @Override
    public String getName() {
        return name;
    }

    public static String moduleId(String inputName) {
        String name = ModelUtils.lower(inputName);
        int pos = name.indexOf(":");
        if (pos <= 0)
            return MODULE_DEFAULT;
        return name.substring(0, pos);
    }

    public static String safeId(String algorithmName, String moduleName) {
        String name = ModelUtils.lower(algorithmName);
        String module = ModelUtils.lower(moduleName);
        int pos = 0;
        while ((pos = name.indexOf(":")) >= 0) {
            String modulePart = name.substring(0, pos);
            if (moduleName != null) {
                if (!module.equals(modulePart)) {
                    throw new IllegalArgumentException("Algorithm [" + algorithmName
                            + "] cannot be part of module [" + moduleName + "]");
                }
            }
            name = name.substring(pos+1);
        }
        return name;
    }

    /**
     * @return true, if the masking algorithm used supports iterations, false otherwise.
     */
    public boolean isIterable() {
        return iterable;
    }

    public Collection<AlgorithmParameter> getParameters() {
        return Collections.unmodifiableCollection( parameters.values() );
    }

    public AlgorithmInfo addParameter(AlgorithmParameter mp) {
        if (mp!=null)
            parameters.put(mp.getId(), mp);
        return this;
    }

    public AlgorithmInfo addParameters(Collection<AlgorithmParameter> mps) {
        if (mps!=null) {
            for (AlgorithmParameter mp : mps) {
                if (mp!=null)
                    parameters.put(mp.getId(), mp);
            }
        }
        return this;
    }

    public AlgorithmParameter getParameter(String name) {
        if (name==null)
            name = "";
        else
            name = name.trim().toLowerCase();
        return parameters.get(name);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 67 * hash + Objects.hashCode(this.name);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final AlgorithmInfo other = (AlgorithmInfo) obj;
        if (this.iterable != other.iterable) {
            return false;
        }
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        if (!Objects.equals(this.parameters, other.parameters)) {
            return false;
        }
        return true;
    }

}
