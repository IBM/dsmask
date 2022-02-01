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
package com.ibm.dsmask.tool;

import com.ibm.dsmask.impl.*;
import groovy.lang.Script;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author zinal
 */
public class GroovyInputLink implements XLinkInput {

    private final Script script;
    private List<XColumnInfo> columns = null;
    private List<DummyInput> data = null;
    private int position = -1;

    public GroovyInputLink(GroovyRunner runner, String fname) {
        this.script = runner.compileFile(fname);
    }

    @Override
    public List<XColumnInfo> getInputColumns() {
        if (columns==null)
            columns = getColumns();
        return columns;
    }

    @Override
    public XRowInput readRecord() {
        if (data==null) {
            data = getData();
            position = 0;
        }
        if (position >= data.size())
            return null;
        return data.get(position++);
    }

    private List<XColumnInfo> getColumns() {
        int index = 0;
        final List<XColumnInfo> work = new ArrayList<>();
        final Object cols = script.invokeMethod("getColumns", null);
        if (cols instanceof List<?>) {
            for (Object o : ((List<?>)cols))
                work.add(new XColumnInfo(++index, o.toString()));
        } else if (cols instanceof Object[]) {
            for (Object o : ((Object[])cols))
                work.add(new XColumnInfo(++index, o.toString()));
        }
        return work;
    }

    private List<DummyInput> getData() {
        final List<DummyInput> work = new ArrayList<>();
        final Object vals = script.invokeMethod("getData", null);
        if (vals instanceof List<?>) {
            for (Object o : ((List<?>)vals))
                work.add(convert(o));
        } else if (vals instanceof Object[]) {
            for (Object o : ((Object[])vals))
                work.add(convert(o));
        }
        return work;
    }

    private DummyInput convert(Object o) {
        final DummyInput v = new DummyInput();
        if (o instanceof List<?>) {
            v.setValues(((List<?>)o).toArray());
        } else if (o instanceof Object[]) {
            v.setValues((Object[])o);
        } else {
            v.setValues(new Object[] { o });
        }
        return v;
    }

}
