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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import org.h2.mvstore.*;

/**
 * A single shard containing reverse and conflict maps.
 * @author zinal
 */
public class UniqShard implements AutoCloseable {

    private static final org.slf4j.Logger LOG
            = org.slf4j.LoggerFactory.getLogger(UniqShard.class);

    public static final String PREFIX_CONFLICT = "C$";
    public static final String PREFIX_REVERSE = "R$";

    private final File file;
    private final int number;
    private final int cacheSize;

    private MVStore store;
    // masked value -> original value (all in packed form)
    private final Map<String, MVMap<String,String>> reverse = new HashMap<>();
    // (masked value + 0x02 + original value) -> iteration
    private final Map<String, MVMap<String,Integer>> conflict = new HashMap<>();

    public UniqShard(File directory, int number, int cacheSize) {
        if (directory != null) {
            this.file = new File(directory, "dsmask_uniq_"
                    + Integer.toHexString(number));
        } else {
            this.file = null;
        }
        this.number = number;
        this.cacheSize = goodCacheSize(cacheSize);
    }

    public File getFile() {
        return file;
    }

    public int getNumber() {
        return number;
    }

    private void openStore() {
        if (store==null) {
            if (file==null) {
                store = new MVStore.Builder()
                        .autoCommitDisabled()
                        .compress()
                        .open();
            } else {
                store = new MVStore.Builder()
                        .fileName(file.getAbsolutePath())
                        .autoCommitDisabled()
                        .compress()
                        .cacheSize(cacheSize)
                        .open();
            }
        }
    }

    private MVMap<String,Integer> openConflict(String type) {
        synchronized(this) {
            MVMap<String,Integer> m = conflict.get(type);
            if (m==null) {
                openStore();
                m = store.openMap(PREFIX_CONFLICT + type);
                conflict.put(type, m);
            }
            return m;
        }
    }

    private MVMap<String,String> openReverse(String type) {
        synchronized(this) {
            MVMap<String,String> m = reverse.get(type);
            if (m==null) {
                openStore();
                m = store.openMap(PREFIX_REVERSE + type);
                reverse.put(type, m);
            }
            return m;
        }
    }

    @Override
    public void close() {
        synchronized(this) {
            if (store != null) {
                store.commit();
                store.close();
                store = null;
            }
            conflict.clear();
            reverse.clear();
        }
    }

    public void open() {
        synchronized(this) {
            openStore();
        }
    }

    public UniqResponse store(UniqShardReq sr) {
        MVMap<String,String> mapRev = openReverse(sr.getRepository());
        String storedSource = mapRev.get(sr.getTarget());
        if (storedSource!=null) {
            if (storedSource.equals(sr.getSource())) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Store MATCH at {} shard {}, [{}] -> [{}], "
                            + "base [{}], iteration {}",
                            sr.getRepository(), number, sr.getSource(), sr.getTarget(),
                            sr.getConflict(), sr.getIteration());
                }
                return new UniqResponse(true);
            }
            if (sr.getIteration()==0) {
                // we have an initial conflict here,
                // so trying to grab the resolution
                int iteration = readHint(sr);
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Store CONFLICT at {} shard {}, [{}] -> [{}], "
                            + "base [{}], iteration {}, hint {}",
                            sr.getRepository(), number, sr.getSource(), sr.getTarget(),
                            sr.getConflict(), sr.getIteration(), iteration);
                }
                return new UniqResponse(false, iteration);
            }
            // non-initial conflict, just reporting
            if (LOG.isDebugEnabled()) {
                LOG.debug("Store CONFLICT at {} shard {}, [{}] -> [{}], "
                        + "base [{}], iteration {}",
                        sr.getRepository(), number, sr.getSource(), sr.getTarget(),
                        sr.getConflict(), sr.getIteration());
            }
            return new UniqResponse(false);
        }
        mapRev.put(sr.getTarget(), sr.getSource());
        if (LOG.isDebugEnabled()) {
            LOG.debug("Store INITIAL at {} shard {}, [{}] -> [{}], "
                    + "base [{}], iteration {}",
                    sr.getRepository(), number, sr.getSource(), sr.getTarget(),
                    sr.getConflict(), sr.getIteration());
        }
        return new UniqResponse(true);
    }

    public int readHint(UniqShardReq sr) {
        MVMap<String,Integer> mapConflict = openConflict(sr.getRepository());
        Integer iteration = mapConflict.get(sr.getResolutionKey());
        if (iteration!=null)
            return iteration;
        return -1;
    }

    public void writeHint(UniqShardReq sr) {
        final String resolutionKey = sr.getResolutionKey();
        MVMap<String,Integer> mapConflict = openConflict(sr.getRepository());
        mapConflict.put(resolutionKey, sr.getIteration());
        if (LOG.isDebugEnabled()) {
            LOG.debug("Hint at {} shard {}, [{}] -> [{}], "
                    + "base [{}], iteration {}, key [{}]",
                    sr.getRepository(), number, sr.getSource(), sr.getTarget(),
                    sr.getConflict(), sr.getIteration(), resolutionKey);
        }
    }

    public void commit() {
        // We do not need synchronization here, as it is ensured in UniqStore.
        if (store!=null && store.hasUnsavedChanges()) {
            store.commit();
            store.compact(50, 16 * 1024 * 1024);
        }
    }

    private static int goodCacheSize(int size) {
        if (size <= 0)
            size = 1;
        else if (size > 100)
            size = 100;
        return size;
    }

    /**
     * Dump the contents of the shard into files in the specified directory
     * @param directory Directory name
     * @throws Exception
     */
    public void dump(File directory) throws Exception {
        // The store can be closed at this point.
        synchronized(this) {
            openStore();
        }
        // Open all the maps
        for (String mapName : new ArrayList<>(store.getMapNames())) {
            if (mapName.startsWith(PREFIX_REVERSE))
                openReverse(mapName.substring(PREFIX_REVERSE.length()));
            else if (mapName.startsWith(PREFIX_CONFLICT))
                openConflict(mapName.substring(PREFIX_CONFLICT.length()));
        }
        final HashSet<String> allTypes = new HashSet<>();
        synchronized(this) {
            allTypes.addAll(conflict.keySet());
            allTypes.addAll(reverse.keySet());
        }
        final String shardNumber = String.valueOf(number);
        File f;
        // Generate the text dump for each map, appending to the output from other shards
        for (String type : allTypes) {
            // Lookup map
            f = new File(directory, "dsmask-uniq-" + type + "-lookup.txt");
            try (BufferedWriter bw = openDumpFile(f)) {
                MVMap<String,String> m = openReverse(type);
                String key = m.firstKey();
                while (key != null) {
                    String value = m.get(key);
                    if (value!=null) {
                        bw.append(value);
                        bw.append("\t");
                        bw.append(key);
                        bw.append("\t");
                        bw.append(shardNumber);
                        bw.newLine();
                    }
                    key = m.higherKey(key);
                }
            }
            // Conflict map
            f = new File(directory, "dsmask-uniq-" + type + "-conflict.txt");
            try (BufferedWriter bw = openDumpFile(f)) {
                MVMap<String,Integer> m = openConflict(type);
                String key = m.firstKey();
                while (key != null) {
                    Integer value = m.get(key);
                    if (value!=null) {
                        String[] parts = key.split(UniqShardReq.SEP);
                        if (parts.length != 2)
                            continue;
                        bw.append(parts[1]);
                        bw.append("\t");
                        bw.append(parts[0]);
                        bw.append("\t");
                        bw.append(String.valueOf(value));
                        bw.append("\t");
                        bw.append(shardNumber);
                        bw.newLine();
                    }
                    key = m.higherKey(key);
                }
            }
        }
    }

    private BufferedWriter openDumpFile(File f) throws Exception {
        return new BufferedWriter(
                    new OutputStreamWriter(
                            new FileOutputStream(f, true), StandardCharsets.UTF_8));
    }

}
