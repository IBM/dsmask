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

/**
 * Identity of a model element - case-insensitive name being unique
 * within its type and scope.
 * @author zinal
 */
public interface ModelIdentity {

    /**
     * @return Name in its original case
     */
    String getName();

    /**
     * @return Name in the normalized form (trimmed lower case)
     */
    String getId();

}
