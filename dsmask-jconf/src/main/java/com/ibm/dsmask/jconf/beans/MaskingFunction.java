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

import com.ibm.dsmask.beans.FunctionType;
import java.util.Objects;

/**
 * A masking function - actual piece of logic to mask data.
 * Used in a masking step to perform actual computations.
 * @author zinal
 */
public class MaskingFunction {

    private String name;
    private FunctionType type;
    private String text;
    private String textInput;

    public MaskingFunction() {
        this.name = Utils.NONE;
    }

    public MaskingFunction(String name, FunctionType type) {
        this.name = Utils.lower(name);
        this.type = type;
    }

    public MaskingFunction(String name, FunctionType type, String text) {
        this.name = Utils.lower(name);
        this.type = type;
        this.text = text;
    }

    public MaskingFunction(String name, FunctionType type,
            String text, String input) {
        this.name = Utils.lower(name);
        this.type = type;
        this.text = text;
        this.textInput = input;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = Utils.lower(name);
    }

    public FunctionType getType() {
        return type;
    }

    public void setType(FunctionType type) {
        this.type = type;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getTextInput() {
        return textInput;
    }

    public void setTextInput(String textInput) {
        this.textInput = textInput;
    }

    public boolean isValid() {
        return name.length() > 0 && type!=null;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + Objects.hashCode(this.name);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final MaskingFunction other = (MaskingFunction) obj;
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        if (!Objects.equals(this.text, other.text)) {
            return false;
        }
        if (this.type != other.type) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "MaskingFunction{" + name + '}';
    }

}
