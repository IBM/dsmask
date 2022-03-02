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

/**
 * Operations provided by the dsmask-uniq service implementation.
 * @author zinal
 */
public interface UniqProvider extends AutoCloseable {

    /**
     * Perform a number of requests in a batch,
     * returning a response per request.
     * @param repository Repository name
     * @param rr Requests
     * @return Responses
     */
    UniqResponse[] store(String repository, UniqRequest[] rr);

    /**
     * Close should not throw.
     */
    @Override
    void close();

}
