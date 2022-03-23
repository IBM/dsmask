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

import java.util.List;

/**
 * Abstract masking rule.
 * Contains a name, ordered list of input references,
 * and ordered list of output references.
 * 
 * @author zinal
 * @since 2020
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

}
