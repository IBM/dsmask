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

import java.nio.charset.Charset;
import java.util.Collections;
import java.util.HashSet;
import java.util.concurrent.ArrayBlockingQueue;

/**
 *
 * @author mzinal
 */
public class NamesSourceV1 implements NamesSource {

    private static final String[] NAMES = new String[] {
        "Alex", "Ustas", "Frodo", "Bilbo", "Zombie"
    };

    private volatile boolean shutdown = false;

    private final byte[] sortSalt1;
    private final NamesData dataMale;
    private final NamesData dataFemale;

    private final HashSet<String> knownNames = new HashSet<>();
    private int duplicateCount = 0;

    private final ArrayBlockingQueue<NamesDataExt> queueMale
            = new ArrayBlockingQueue<>(100);
    private final ArrayBlockingQueue<NamesDataExt> queueFemale
            = new ArrayBlockingQueue<>(100);

    private NamesDataExt curMale = null;
    private NamesDataExt curFemale = null;

    public NamesSourceV1(String globalSalt, NamesData dataMale,
            NamesData dataFemale) throws Exception {
        this.sortSalt1 = globalSalt.getBytes("UTF-8");
        this.dataMale = dataMale;
        this.dataFemale = dataFemale;
    }

    private synchronized void setShutdown(boolean v) {
        this.shutdown = v;
    }

    private synchronized boolean isShutdown() {
        return this.shutdown;
    }

    @Override
    public int getDuplicateCount() {
        return duplicateCount;
    }

    public byte[] getSortSalt1() {
        return sortSalt1;
    }

    @Override
    public void start() throws Exception {
        setShutdown(false);
        NamesBuilder nb;
        Thread t;
        for (String name : NAMES) {
            nb = new NamesBuilder(this, queueMale, dataMale, name + "_M");
            t = new Thread(nb);
            t.setDaemon(true);
            t.setName(nb.identity);
            t.start();
            nb = new NamesBuilder(this, queueFemale, dataFemale, name + "_F");
            t = new Thread(nb);
            t.setDaemon(true);
            t.setName(nb.identity);
            t.start();
        }
    }

    @Override
    public void stop() {
        setShutdown(true);
        queueMale.clear();
        queueFemale.clear();
    }

    @Override
    public NameValues nextMale() throws Exception {
        if (curMale == null) {
            curMale = queueMale.take();
        }
        NameValues val = null;
        while (val == null) {
            val = curMale.next();
            if (val == null) {
                curMale = queueMale.take();
            }
            if (val != null) {
                if (! knownNames.add(val.full.toLowerCase())) {
                    // duplicate value
                    val = null;
                    ++ duplicateCount;
                }
            }
        }
        return val;
    }

    @Override
    public NameValues nextFemale() throws Exception {
        if (curFemale == null) {
            curFemale = queueFemale.take();
        }
        NameValues val = null;
        while (val == null) {
            val = curFemale.next();
            if (val == null) {
                curFemale = queueFemale.take();
            }
            if (val != null) {
                if (! knownNames.add(val.full.toLowerCase())) {
                    // duplicate value
                    val = null;
                    ++ duplicateCount;
                }
            }
        }
        return val;
    }

    private static class NamesDataExt extends NamesData {
        public int posDual = 0;
        public int posMiddle = 0;

        public NamesDataExt(NamesData nd) {
            super(nd);
        }

        public final NameValues next() {
            if (posDual >= first.size() || posDual >= last.size()) {
                posDual = 0;
                posMiddle++;
            }
            if (posMiddle >= middle.size()) {
                return null; // no more values
            }
            NameValues nv = new NameValues(
                    first.get(posDual).getName(),
                    last.get(posDual).getName(),
                    middle.get(posMiddle).getName()
                );
            posDual++;
            return nv;
        }
    }

    private static class NamesBuilder implements Runnable {

        private final NamesSourceV1 controller;
        private final ArrayBlockingQueue<NamesDataExt> queue;
        private final NamesData original;
        private final String identity;
        private final Charset cset = Charset.forName("UTF-8");

        private int sortIteration = 0;
        private byte[] sortSalt2;

        public NamesBuilder(NamesSourceV1 controller,
                ArrayBlockingQueue<NamesDataExt> queue,
                NamesData original, String identity) throws Exception {
            this.controller = controller;
            this.queue = queue;
            this.original = original;
            this.identity = identity;
        }

        @Override
        public void run() {
            while (!controller.isShutdown()) {
                sortIteration++;
                sortSalt2 = (String.valueOf(sortIteration) + "-"
                        + identity + "-"
                        + (System.currentTimeMillis() % 700))
                        .getBytes(cset);
                final NamesComparator comp = new NamesComparator(
                        controller.getSortSalt1(), sortSalt2);
                NamesDataExt nd = new NamesDataExt(original);
                Collections.sort(nd.first, comp);
                Collections.sort(nd.last, comp);
                Collections.sort(nd.middle, comp);
                while (true) {
                    try {
                        queue.put(nd);
                        break;
                    } catch(InterruptedException ix) {
                        if (controller.isShutdown())
                            break;
                    }
                }
            }
        }

    }

    @Override
    public String toString() {
        return "NamesSourceV1";
    }

}
