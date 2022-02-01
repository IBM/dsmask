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
package net.dsmask.model.common;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import net.dsmask.model.any.Utils;
import net.dsmask.model.algo.AlgorithmInfo;

/**
 * A masking function combines the masking algorithm with its parameters.
 * Used in a masking step to perform actual computations.
 * @author zinal
 */
public class MaskingFunction extends EntityBase {

    private final AlgorithmInfo algorithm;
    private final Map<String, String> parameters = new HashMap<>();

    public MaskingFunction(String name, AlgorithmInfo algorithm) {
        super(EntityType.Function, name);
        this.algorithm = algorithm;
    }

    public AlgorithmInfo getAlgorithm() {
        return algorithm;
    }

    public Map<String, String> getParameters() {
        return Collections.unmodifiableMap(parameters);
    }

    public MaskingFunction setParameter(String name, String value) {
        MaskingParameter mp = algorithm.getParameter(name);
        if (mp==null) {
            throw new IllegalArgumentException("Illegal parameter [" + name
                    + "] for algorithm [" + algorithm.getName() + "]");
        }
        if (value==null)
            parameters.remove(mp.getId());
        else
            parameters.put(mp.getId(), value);
        return this;
    }

    /**
     * @return true, if the masking algorithm used supports iterations, false otherwise.
     */
    public boolean isIterable() {
        return algorithm.isIterable();
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
        final MaskingFunction other = (MaskingFunction) obj;
        if (!Objects.equals(this.algorithm, other.algorithm)) {
            return false;
        }
        if (this.parameters.size() != other.parameters.size()) {
            return false;
        }
        for (Map.Entry<String, String> me : this.parameters.entrySet()) {
            String otherValue = other.parameters.get(me.getKey());
            if (!Utils.equalsNL(me.getValue(), otherValue)) {
                return false;
            }
        }
        return true;
    }

}
