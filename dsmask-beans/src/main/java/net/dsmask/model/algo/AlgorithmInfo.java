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
package net.dsmask.model.algo;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Objects;
import net.dsmask.model.any.Utils;
import net.dsmask.model.common.*;

/**
 * Data masking algorithm used to implement the data masking function.
 * @author zinal
 */
public class AlgorithmInfo implements ModelIdentity {

    public static String MODULE_DEFAULT = "koodaus";

    private final String name;
    private final boolean iterable;
    private final LinkedHashMap<String, MaskingParameter> parameters = new LinkedHashMap<>();

    public AlgorithmInfo(String name) {
        this(name, false);
    }

    public AlgorithmInfo(String name, boolean iterable) {
        this.name = Utils.safe(name);
        this.iterable = iterable;
    }

    @Override
    public String getId() {
        return Utils.lower(name);
    }

    @Override
    public String getName() {
        return name;
    }

    public static String moduleId(String inputName) {
        String name = Utils.lower(inputName);
        int pos = name.indexOf(":");
        if (pos <= 0)
            return MODULE_DEFAULT;
        return name.substring(0, pos);
    }

    public static String safeId(String inputName, String inputModule) {
        String name = Utils.lower(inputName);
        String module = Utils.lower(inputModule);
        int pos = 0;
        while ((pos = name.indexOf(":")) >= 0) {
            String modulePart = name.substring(0, pos);
            if (inputModule != null) {
                if (!module.equals(modulePart)) {
                    throw new IllegalArgumentException("Algorithm [" + inputName
                            + "] cannot be part of module [" + inputModule + "]");
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

    public Collection<MaskingParameter> getParameters() {
        return Collections.unmodifiableCollection(parameters.values());
    }

    public AlgorithmInfo addParameter(MaskingParameter mp) {
        if (mp!=null)
            parameters.put(mp.getId(), mp);
        return this;
    }

    public AlgorithmInfo addParameters(Collection<MaskingParameter> mps) {
        if (mps!=null) {
            for (MaskingParameter mp : mps) {
                if (mp!=null)
                    parameters.put(mp.getId(), mp);
            }
        }
        return this;
    }

    public MaskingParameter getParameter(String name) {
        if (name==null)
            name = "";
        else
            name = name.trim().toLowerCase();
        return parameters.get(name);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (! super.equals(obj)) {
            return false;
        }
        final AlgorithmInfo other = (AlgorithmInfo) obj;
        if (this.iterable != other.iterable) {
            return false;
        }
        if (!Objects.equals(this.parameters, other.parameters)) {
            return false;
        }
        return true;
    }

}
