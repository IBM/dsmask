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
 * Masking function bean
 * @author zinal
 */
public class MskFunc {

    private int id;
    private String name;
    private FunctionType functionType;
    private String text;
    private boolean iterable;

    public MskFunc() {
    }

    public MskFunc(int id, String name, FunctionType functionType) {
        this.id = id;
        this.name = name;
        this.functionType = functionType;
    }

    public MskFunc(int id, String name, FunctionType functionType, String text) {
        this.id = id;
        this.name = name;
        this.functionType = functionType;
        this.text = text;
    }

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

    public FunctionType getFunctionType() {
        return functionType;
    }

    public void setFunctionType(FunctionType functionType) {
        this.functionType = functionType;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean isIterable() {
        return iterable;
    }

    public void setIterable(boolean iterable) {
        this.iterable = iterable;
    }

}
