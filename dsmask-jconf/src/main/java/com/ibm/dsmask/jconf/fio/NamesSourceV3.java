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
import java.util.concurrent.ArrayBlockingQueue;

/**
 *
 * @author mzinal
 */
public class NamesSourceV3 implements NamesSource {

    private static final String[] NAMES = new String[] {
        "Alex", "Ustas", "Frodo", "Bilbo", "Zombie"
    };

    private final NamesData dataMale;
    private final NamesData dataFemale;

    private final HashSet<String> knownNames = new HashSet<>();
    private int duplicateCount = 0;

    private volatile boolean shutdown = false;

    private final GenContext genMale = new GenContext();
    private final GenContext genFemale = new GenContext();

    public NamesSourceV3(String salt, NamesData dataMale,
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
        setShutdown(false);
        NamesData[] arrayMale = dataMale.split(NAMES.length);
        NamesData[] arrayFemale = dataFemale.split(NAMES.length);
        NamesBuilder nb;
        Thread t;
        int position = 0;
        for (String name : NAMES) {
            nb = new NamesBuilder(this, genMale.queue, arrayMale[position]);
            t = new Thread(nb);
            t.setName("Gen_"+name+"_M");
            t.setDaemon(true);
            t.start();
            nb = new NamesBuilder(this, genFemale.queue, arrayFemale[position]);
            t = new Thread(nb);
            t.setName("Gen_"+name+"_F");
            t.setDaemon(true);
            t.start();
            ++position;
        }
    }

    @Override
    public void stop() {
        setShutdown(true);
        genMale.queue.clear();
        genFemale.queue.clear();
    }

    private synchronized void setShutdown(boolean v) {
        this.shutdown = v;
    }

    private synchronized boolean isShutdown() {
        return this.shutdown;
    }

    @Override
    public NameValues nextMale() throws Exception {
        return genMale.next();
    }

    @Override
    public NameValues nextFemale() throws Exception {
        return genFemale.next();
    }

    private static NameValues nextOne(NamesData data, Positions pos) {
        NameValues val = new NameValues(
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
                    /* Overlap! */
                }
            }
        }
        return val;
    }

    private static NamesBlock nextBlock(NamesData data, Positions pos) {
        final NamesBlock blk = new NamesBlock();
        for (int i=0; i<NamesBlock.SZ; ++i) {
            blk.values[i] = nextOne(data, pos);
            blk.keys[i] = blk.values[i].full.toLowerCase();
        }
        return blk;
    }

    static class Positions {
        int first = 0;
        int middle = 0;
        int last = 0;
    }

    static class NamesBlock {
        static final int SZ = 1000;
        final NameValues[] values = new NameValues[SZ];
        final String[] keys = new String[SZ];
        int position = -1;

        final boolean next() {
            if (position < SZ)
                ++position;
            return (position>=0 && position<SZ);
        }

        final NameValues getValue() {
            if (position >= SZ)
                return null;
            return values[position];
        }

        final String getKey() {
            if (position >= SZ)
                return null;
            return keys[position];
        }
    }

    final class GenContext {
        final ArrayBlockingQueue<NamesBlock> queue = new ArrayBlockingQueue<>(100);
        NamesBlock block = null;

        private NameValues next() {
            NameValues nv = null;
            while (nv == null) {
                while (block == null) {
                    try {
                        block = queue.take();
                    } catch(InterruptedException ix) {
                        if (isShutdown())
                            return null;
                    }
                }
                if (!block.next()) {
                    block = null;
                    nv = null;
                } else {
                    nv = block.getValue();
                    if (!knownNames.add(block.getKey())) {
                        nv = null;
                        ++duplicateCount;
                    }
                }
            }
            return nv;
        }
    }

    private static class NamesBuilder implements Runnable {
        final NamesSourceV3 owner;
        final ArrayBlockingQueue<NamesBlock> queue;
        final NamesData data;
        final Positions pos;

        public NamesBuilder(NamesSourceV3 owner,
                ArrayBlockingQueue<NamesBlock> queue,
                NamesData data) {
            this.owner = owner;
            this.queue = queue;
            this.data = data;
            this.pos = new Positions();
        }

        @Override
        public void run() {
            while (!owner.isShutdown()) {
                final NamesBlock nb = nextBlock(data, pos);
                while (true) {
                    try {
                        queue.put(nb);
                        break;
                    } catch(InterruptedException ix) {
                        if (owner.isShutdown())
                            break;
                    }
                }
            }
        }

    }

    @Override
    public String toString() {
        return "NamesSourceV3";
    }

}
