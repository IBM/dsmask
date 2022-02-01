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
package com.ibm.dsmask.uniq;

import com.ibm.dsmask.util.DsMaskUtil;

/**
 * Shard-optimized request for uniqueness checking.
 * @author zinal
 */
public class UniqShardReq {

    public static final String SEP = DsMaskUtil.fromCodepoint(3);

    private final String repository;
    private final String source;
    private final String target;
    private final String conflict;
    private final int iteration;

    public UniqShardReq(String repository, UniqRequest r) {
        this.repository = repository;
        this.source = r.getSource();
        this.target = r.getTarget();
        this.conflict = r.getConflict();
        this.iteration = r.getIteration();
    }

    public String getRepository() {
        return repository;
    }

    public String getSource() {
        return source;
    }

    public String getTarget() {
        return target;
    }

    public String getConflict() {
        return conflict;
    }

    public int getIteration() {
        return iteration;
    }

    public String getResolutionKey() {
        if (conflict == null)
            return target + SEP + source;
        return conflict + SEP + source;
    }

}
