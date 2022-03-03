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
 * Interface to retrieve the set of masking rules.
 * 
 * @author zinal
 * @since 2020
 */
public interface RulesAccessor {

    List<? extends AnyRule> retrieveRules(String context);

}
