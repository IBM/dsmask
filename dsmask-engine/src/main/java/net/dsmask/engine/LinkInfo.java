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
package net.dsmask.engine;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author zinal
 */
public class LinkInfo {

    private final LinkInput inputLink;
    private final LinkOutput outputLink;
    private final Map<String, Integer> inputMap;
    private final Map<String, Integer> outputMap;

    public LinkInfo(LinkInput input, LinkOutput output) {
        this.inputLink = input;
        this.outputLink = output;
        this.inputMap = map(input.getInputColumns());
        this.outputMap = map(input.getInputColumns());
    }

    public LinkInput getInputLink() {
        return inputLink;
    }

    public LinkOutput getOutputLink() {
        return outputLink;
    }

    public Map<String, Integer> getInputMap() {
        return inputMap;
    }

    public Map<String, Integer> getOutputMap() {
        return outputMap;
    }

    private static Map<String, Integer> map(List<XColumnInfo> columns) {
        final Map<String, Integer> m = new HashMap<>();
        for (XColumnInfo ci : columns)
            m.put(ci.getName(), ci.getIndex());
        return m;
    }

}
