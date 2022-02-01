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
package com.ibm.dsmask.jconf.fio;

import java.util.HashSet;

/**
 *
 * @author mzinal
 */
public class NamesSourceV2 implements NamesSource {

    private final NamesData dataMale;
    private final NamesData dataFemale;

    private final Positions posMale = new Positions();
    private final Positions posFemale = new Positions();

    private boolean antiDupProtection = false;
    private final HashSet<String> knownNames = new HashSet<>();
    private int duplicateCount = 0;

    public NamesSourceV2(String salt, NamesData dataMale,
            NamesData dataFemale) throws Exception {
        final NamesSorter ns = new NamesSorter(salt);
        this.dataMale = ns.sort(dataMale);
        this.dataFemale = ns.sort(dataFemale);
    }

    @Override
    public int getDuplicateCount() {
        return duplicateCount;
    }

    @Override
    public void start() throws Exception {
    }

    @Override
    public void stop() {
    }

    @Override
    public NameValues nextMale() throws Exception {
        return next(dataMale, posMale);
    }

    @Override
    public NameValues nextFemale() throws Exception {
        return next(dataFemale, posFemale);
    }

    private NameValues next(NamesData data, Positions pos) throws Exception {
        NameValues val = null;
        while (val == null) {
            val = new NameValues(
                data.first.get(pos.first).getName(),
                data.last.get(pos.last).getName(),
                data.middle.get(pos.middle).getName()
            );
            pos.last += 1;
            if (pos.last >= data.last.size()) {
                pos.last = 0;
                pos.first += 1;
                if (pos.first >= data.first.size()) {
                    pos.first = 0;
                    pos.middle += 1;
                    if (pos.middle >= data.middle.size()) {
                        pos.middle = 0;
                        throw new Exception("Indexes overlapped, "
                                + "not enough input dictionary values");
                    }
                }
            }
            if (antiDupProtection) {
                if (! knownNames.add(val.full.toLowerCase()) ) {
                    val = null;
                    ++duplicateCount;
                }
            }
        }
        return val;
    }

    static class Positions {
        int first = 0;
        int middle = 0;
        int last = 0;
    }

    @Override
    public String toString() {
        return "NamesSourceV2";
    }

}
