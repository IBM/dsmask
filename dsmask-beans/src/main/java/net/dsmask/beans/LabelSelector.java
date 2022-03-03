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
package net.dsmask.beans;

import java.util.ArrayList;
import java.util.List;

/**
 * Masking label selector based on the defined public tags.
 * @author zinal
 */
public class LabelSelector extends EntityBase {

    private final List<Item> items = new ArrayList<>();

    public LabelSelector(String name) {
        super(EntityType.Selector, name);
    }

    public List<Item> getItems() {
        return items;
    }

    public static class Item {
        public final String source;
        public final String target;
        public final boolean rx;

        public Item(String source, String target, boolean rx) {
            this.source = source;
            this.target = target;
            this.rx = rx;
        }
    }

}
