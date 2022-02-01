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
package com.ibm.dsmask.beans;

import java.util.ArrayList;
import java.util.List;

/**
 * Masking rule bean.
 * @author zinal
 */
public class MskRule {

    private int id;
    private String name;
    private final List<MskStep> steps = new ArrayList<>();

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<MskStep> getSteps() {
        return steps;
    }

    @Override
    public String toString() {
        return "MskRule{" + "id=" + id + ", name=" + name + '}';
    }

}
