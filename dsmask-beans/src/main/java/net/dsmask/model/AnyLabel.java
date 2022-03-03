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

/**
 * Generic abstraction of a masking label.
 * 
 * @author zinal
 * @since 2020
 */
public interface AnyLabel {
    
    /**
     * @return Label identifier (case-insensitive).
     */
    String getId();

    /**
     * @return Label mode.
     */
    LabelMode getMode();
    
    /**
     * @return true, if mode is Confidential, false otherwise
     */
    boolean isConfidential();
    
    /**
     * @return true, if mode is Group, false otherwise
     */
    boolean isGroup();
    
}
