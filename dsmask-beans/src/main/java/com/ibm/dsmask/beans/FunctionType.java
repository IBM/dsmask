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

/**
 * Type of a data masking function.
 * @author zinal
 */
public enum FunctionType {
    /** No-op: output=input. Used to project the results of the previous steps. */
    Project,
    /** Concatenate inputs into a single string. */
    Concat,
    /** Split the string into peaces. */
    Split,
    /** Built-in date operation. */
    DateOp,
    /** Built-in string operation. */
    StringOp,
    /** Integer hash computation. */
    NumberHash,
    /** Message digest computation. */
    DigestHash,
    /** Character substitution. */
    CharSubst,
    /** Hash-based lookup in a search table. */
    HashLookup,
    /** Key-based lookup in a search table. */
    KeyLookup,
    /** Format preserving encryption / hashing. */
    FPE,
    /** Call the Groovy script. */
    GroovyScript,
    /** Call the Optim Data Privacy Providers masking function. */
    ODPP;

    public static FunctionType fromCode(String code) {
        if (code==null)
            return GroovyScript;
        String xcode = code.trim();
        if (xcode.length()==0)
            return GroovyScript;
        for (FunctionType ft : FunctionType.values()) {
            if (ft.name().equalsIgnoreCase(xcode))
                return ft;
        }
        xcode = xcode.toLowerCase();
        for (FunctionType ft : FunctionType.values()) {
            if (ft.name().toLowerCase().startsWith(xcode))
                return ft;
        }
        throw new IllegalArgumentException("Unknown masking function type: " + code);
    }
}
