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

import java.util.List;

/**
 * Group transformation steps include @StepRoot and @StepBlock.
 * @author zinal
 */
public interface StepGroup extends StepAny {

    /**
     * @return Sequence of steps
     */
    List<StepBase> getItems();

    /**
     * Add step to the group
     * @param sb Step to be added
     * @return Step group
     */
    StepGroup addItem(StepBase sb);

}
