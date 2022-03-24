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

import java.util.Set;

/**
 * Reference to a masking label.
 * Can point to one or more masking label(s).
 * 
 * @author zinal
 * @since 2020
 */
public interface AnyReference {
    
    /**
     * @return Unmodifiable collection of masking labels.
     */
    Set<? extends AnyLabel> getLabels();
    
}