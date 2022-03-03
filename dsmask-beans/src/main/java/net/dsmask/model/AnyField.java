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

/**
 * Generic table field abstraction.
 * Contains the name plus the collection of masking labels.
 * 
 * @author zinal
 * @since 2020
 */
public interface AnyField {

    String getName();
    
    /**
     * @return Unmodifiable collection of masking labels
     */
    Collection<? extends AnyLabel> getLabels();
    
    /**
     * @return true, if the field is confidential,
     *         e.g. if it has at least one confidential label
     */
    boolean isConfidential();
    
}
