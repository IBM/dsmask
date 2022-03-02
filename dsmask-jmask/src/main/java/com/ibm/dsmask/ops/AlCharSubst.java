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
package com.ibm.dsmask.ops;

import java.util.List;
import net.dsmask.algo.CharzTable;
import net.dsmask.algo.CharzTranslate;
import com.ibm.dsmask.beans.*;
import com.ibm.dsmask.impl.Utils;
import com.ibm.dsmask.impl.XKeeper;

/**
 * Character substitution according to the translation table specified.
 * @author mzinal
 */
public class AlCharSubst implements AlSimpleValue {

    private final CharzTranslate algo;

    public AlCharSubst(XKeeper keeper, MskFunc func) {
        List<String[]> conf = Utils.parseConfig(func.getText());

        CharzTable charzTable = keeper.getCharTable(
                Utils.getConfigValue(conf, "table"));

        this.algo = new CharzTranslate(charzTable);
    }

    public CharzTranslate getAlgo() {
        return algo;
    }

    @Override
    public Object exec(Object in) {
        if (in==null)
            return null;
        return algo.translate(in.toString());
    }

    @Override
    public boolean isIterationsSupported() {
        return false;
    }

    @Override
    public Object exec(Object in, int iteration) {
        if (in==null)
            return null;
        return algo.translate(in.toString());
    }

}
