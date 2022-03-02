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
package net.dsmask.uniq;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lazy background commit control thread.
 * @author zinal
 */
public class UniqLazyCommit implements Runnable {

    private static final org.slf4j.Logger LOG
            = org.slf4j.LoggerFactory.getLogger(UniqLazyCommit.class);

    private final UniqStore store;
    private final Map<Integer, Long> firstChangeTime = new HashMap<>();
    private volatile long currentTime = 0L;

    private final Object guard = new Object();
    private volatile boolean timeToQuit = false;

    private final long commitAfter;
    private int commitCount = 0;

    public UniqLazyCommit(UniqStore store, long commitAfter) {
        this.store = store;
        this.commitAfter = (commitAfter >= 1000L) ? commitAfter : 5000L;
    }

    public boolean isTimeToQuit() {
        synchronized(guard) {
            return timeToQuit;
        }
    }

    public void resetQuit() {
        synchronized(guard) {
            timeToQuit = false;
        }
    }

    public void tellQuit() {
        synchronized(guard) {
            timeToQuit = true;
        }
    }

    public void hasChanges(int shard) {
        synchronized(firstChangeTime) {
            if (firstChangeTime.get(shard) == null)
                firstChangeTime.put(shard, currentTime);
        }
    }

    public void hasChanges(List<Integer> shards) {
        synchronized(firstChangeTime) {
            for (Integer shard : shards) {
                if (firstChangeTime.get(shard) == null)
                    firstChangeTime.put(shard, currentTime);
            }
        }
    }

    @Override
    public void run() {
        resetQuit();
        commitCount = 0;
        LOG.info("Commit thread running with delay {} msec.", commitAfter);
        while (isTimeToQuit()==false) {
            // Grab the changes data
            final Map<Integer, Long> times;
            synchronized(firstChangeTime) {
                times = new HashMap<>(firstChangeTime);
                firstChangeTime.clear();
                currentTime = System.currentTimeMillis();
            }
            // Commit the shards with changes
            final long tv = System.currentTimeMillis();
            final List<Integer> pending = new ArrayList<>();
            for (Map.Entry<Integer,Long> me : times.entrySet()) {
                if ( (tv - me.getValue()) >= commitAfter ) {
                    store.commitShard(me.getKey());
                    pending.add(me.getKey());
                    ++commitCount;
                }
            }
            // Exclude already-committed shards
            for (Integer key : pending)
                times.remove(key);
            // Return the non-committed data to the map
            if (times.isEmpty()==false)
                synchronized(firstChangeTime) {
                    for (Map.Entry<Integer,Long> me : times.entrySet()) {
                        // Old value will always be smaller
                        firstChangeTime.put(me.getKey(), me.getValue());
                    }
                }
            try {
                Thread.sleep(300L);
            } catch(InterruptedException ix) {}
        }
        LOG.info("Commit thread terminating, total commits: {}", commitCount);
    }

}
