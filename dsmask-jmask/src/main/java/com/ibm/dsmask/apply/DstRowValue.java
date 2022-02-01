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
package com.ibm.dsmask.apply;

import java.util.Collections;
import java.util.Map;

/**
 * The current row for DsApply operator.
 * @author mzinal
 */
public class DstRowValue {

    private String tableName;
    private Map<String, Object> before = null;
    private Map<String, Object> after = null;

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public Map<String, Object> getBefore() {
        if (before==null)
            return Collections.emptyMap();
        return before;
    }

    public Map<String, Object> getAfter() {
        if (after==null)
            return Collections.emptyMap();
        return after;
    }

    public Object getBeforeVal(String name) {
        if (before==null)
            return null;
        return before.get(name);
    }

    public Object getAfterVal(String name) {
        if (after==null)
            return null;
        return after.get(name);
    }

    public void parse(String tableName, byte[] before, byte[] after)
            throws Exception {
        this.tableName = tableName;
        this.before = DstMergeAlgo.unpack(before);
        this.after = DstMergeAlgo.unpack(after);
    }

}
