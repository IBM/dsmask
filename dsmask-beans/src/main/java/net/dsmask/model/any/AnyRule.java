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
package net.dsmask.model.any;

import java.util.List;

/**
 * Abstract masking rule.
 * Contains a name, ordered list of input references,
 * and ordered list of output references.
 * @author zinal
 */
public interface AnyRule {
    
    /**
     * Masking rule name accessor.
     * @return Masking rule name.
     */
    String getName();
    
    /**
     * Retrieves a read-only list of input references.
     * @return List of input references
     */
    List<? extends AnyReference> getInputs();
    
    /**
     * Retrieves a read-only list of output references.
     * @return List of output references
     */
    List<? extends AnyReference> getOutputs();
    
    /**
     * Retrieves an input reference at the specified position.
     * @param pos Position number, zero-based.
     * @return Input reference.
     */
    AnyReference getInput(int pos);
    
    /**
     * Retrieves an output reference at the specified position.
     * @param pos Position number, zero-based.
     * @return Output reference.
     */
    AnyReference getOutput(int pos);
    
}
