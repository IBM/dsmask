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
package com.ibm.dsmask.jconf.beans;

/**
 * Predicate function.
 * Currently used in MaskingStep to skip unnecessary computations.
 */
public class MaskingPredicate {

    private String predicateText;
    private String predicateInput;

    public MaskingPredicate() {
    }

    public String getText() {
        return predicateText;
    }

    public void setText(String predicateText) {
        this.predicateText = predicateText;
    }

    public String getInput() {
        return predicateInput;
    }

    public void setInput(String predicateInput) {
        this.predicateInput = predicateInput;
    }

} // class Predicate
