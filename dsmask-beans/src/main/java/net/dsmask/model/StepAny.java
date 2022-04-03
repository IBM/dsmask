/*
 * Copyright (c) IBM Corp. 2018, 2022.
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
 * Any transformation step definition implements this interface.
 * @author zinal
 */
public interface StepAny {

    /**
     * @return Id of an actual type of item, for simpler handling.
     */
    StepType getType();

    /**
     * @return true, if masking iterations are supported, false otherwise.
     */
    boolean isIterable();

    /**
     * @return Step owner, which must be the group of steps
     */
    StepGroup getOwner();

    /**
     * @param name Name of the item
     * @return The referenced already computed item with the specified name
     */
    StepAny findItem(String name);

}
