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

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import net.dsmask.algo.PureJavaCrc32;

/**
 * Uniqueness validation store implementation.
 * Contains multiple shards, each having its own set
 * of forward and reverse lookup maps.
 * @author zinal
 */
public class UniqStore implements UniqProvider {

    private final UniqShard[] shards;
    private final UniqLazyCommit lazyCommit;
    private Thread lcThread;

    public UniqStore(String directory, int shardCount, int cacheSize, int lazyCommitPeriod) {
        this(directory==null ? (File) null : new File(directory),
                shardCount, cacheSize, lazyCommitPeriod);
    }

    public UniqStore(File directory, int shardCount, int cacheSize, int lazyCommitPeriod) {
        if (directory!=null) {
            if (directory.exists() == false)
                directory.mkdirs();
        }
        if (shardCount < 1)
            shardCount = 1;
        else if (shardCount > 1000)
            shardCount = 1000;
        this.shards = new UniqShard[shardCount];
        for (int i=0; i<shardCount; ++i)
            this.shards[i] = new UniqShard(directory, i, cacheSize);
        this.lazyCommit = new UniqLazyCommit(this, 1000L * lazyCommitPeriod);
    }

    @Override
    public UniqResponse[] store(String repository, UniqRequest[] rr) {
        if (rr==null)
            return null;
        final List<Integer> shardNumbers = new ArrayList<>();
        final UniqResponse[] response = new UniqResponse[rr.length];
        for (int i=0; i<rr.length; ++i) {
            UniqRequest r = rr[i];
            if (r==null) {
                response[i] = null;
                continue;
            }
            UniqShardReq sr = new UniqShardReq(repository, r);
            UniqShard shard = getShard(sr.getTarget());
            shardNumbers.add(shard.getNumber());
            final UniqResponse ur;
            synchronized(shard) {
                ur = shard.store(sr);
            }
            if (ur.isLinkedCorrectly()
                    && sr.getConflict() != null) {
                shard = getShard(sr.getConflict());
                shardNumbers.add(shard.getNumber());
                synchronized(shard) {
                    shard.writeHint(sr);
                }
            }
            response[i] = ur;
        }
        lazyCommit.hasChanges(shardNumbers);
        return response;
    }

    /**
     * Dump all shards into the files in the specified directory.
     * @param directory The directory do dump the data into
     * @throws Exception
     */
    public void dump(File directory) throws Exception {
        for (UniqShard us : shards) {
            us.dump(directory);
        }
    }

    private UniqShard getShard(String v) {
        long hash = getHash(v);
        hash = hash % shards.length;
        return shards[(int) hash];
    }

    private static long getHash(String v) {
        PureJavaCrc32 crc = new PureJavaCrc32();
        crc.update(v.getBytes(StandardCharsets.UTF_8));
        return crc.getValue();
    }

    public void open() {
        for (UniqShard us : shards)
            us.open();
        synchronized(lazyCommit) {
            if (lcThread==null) {
                lcThread = new Thread(lazyCommit);
                lcThread.setDaemon(true);
                lcThread.setName("uniq-store-lazy");
                lcThread.start();
            }
        }
    }

    @Override
    public void close() {
        synchronized(lazyCommit) {
            if (lcThread != null) {
                lazyCommit.tellQuit();
                try { lcThread.join(); } catch (InterruptedException ix) {}
                lcThread = null;
            }
        }
        for (UniqShard us : shards)
            us.close();
    }

    public void commitShard(int shardNumber) {
        final UniqShard shard = shards[shardNumber];
        synchronized(shard) {
            shard.commit();
        }
    }

}
