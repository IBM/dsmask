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

/**
 *
 * @author zinal
 */
public class UniqStoreFactory implements UniqProviderFactory {

    private File file = null;
    private int shardCount = 1;
    private int cacheSize = 10;
    private int commitPeriod = 10;

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public int getShardCount() {
        return shardCount;
    }

    public void setShardCount(int shardCount) {
        this.shardCount = shardCount;
    }

    public int getCacheSize() {
        return cacheSize;
    }

    public void setCacheSize(int cacheSize) {
        this.cacheSize = cacheSize;
    }

    public int getCommitPeriod() {
        return commitPeriod;
    }

    public void setCommitPeriod(int commitPeriod) {
        this.commitPeriod = commitPeriod;
    }

    @Override
    public UniqProvider createProvider() {
        return new UniqStore(file, shardCount, cacheSize, commitPeriod);
    }

}
