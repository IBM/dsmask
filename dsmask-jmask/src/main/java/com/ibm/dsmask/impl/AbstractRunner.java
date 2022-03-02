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
package com.ibm.dsmask.impl;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import net.dsmask.util.ByteArrayWrapper;
import net.dsmask.algo.BasicHasher;
import com.ibm.dsmask.SafeLogger;

/**
 * Common stuff for script runners.
 * Includes just log suppression logic for script logging API.
 * Handles the iteration counter (accessible by the scripts).
 * @author zinal
 */
public abstract class AbstractRunner {

    // message logging limiter
    private final Map<String, MessageInfo> messageInfo = new HashMap<>();
    // digest for message uniqueness
    private final MessageDigest digest;
    // algorithm iteration
    private int currentIteration = 0;

    public AbstractRunner() {
        try {
            this.digest = MessageDigest.getInstance("SHA-512");
        } catch(Exception ex) {
            throw new RuntimeException("Failed to initialize SHA-512", ex);
        }
    }

    public int getCurrentIteration() {
        return currentIteration;
    }

    public void setCurrentIteration(int currentIteration) {
        this.currentIteration = currentIteration;
    }

    public long numberHash(long from, long to, Object ... values) {
        if (from > to) {
            long x = from;
            from = to;
            to = x;
        }
        final long value = new BasicHasher().calcHash(values, currentIteration);
        long range = to - from + 1;
        return from + ( value % range );
    }

    public boolean logMessage(String msgType, int maxCount, String msgText) {
        if (msgType==null || maxCount<1) {
            return false;
        }
        MessageInfo mi = messageInfo.get(msgType);
        if (mi==null) {
            mi = new MessageInfo(msgType, maxCount);
            messageInfo.put(msgType, mi);
        }
        if (mi.currentCount >= mi.maxCount) {
            return false;
        }
        final ByteArrayWrapper baw = new ByteArrayWrapper(
                digest.digest(msgText.getBytes(StandardCharsets.UTF_8))
        );
        if (!mi.hashes.add(baw))
            return false;
        final StringBuilder sb = new StringBuilder();
        sb.append("[").append(msgType).append("] ").append(msgText);
        if (++mi.currentCount >= mi.maxCount) {
            sb.append("\n").append("[").append(msgType)
                    .append("] ** Further messages suppressed");
        }
        SafeLogger.information(sb.toString());
        return true;
    }

    public boolean logOnce(String msgType, String msgText) {
        return logMessage(msgType, 1, msgText);
    }

    public static final class MessageInfo {
        final String msgType;
        final int maxCount;
        final HashSet<ByteArrayWrapper> hashes = new HashSet<>();
        int currentCount;

        public MessageInfo(String msgType, int maxCount) {
            this.msgType = msgType;
            this.maxCount = maxCount;
            this.currentCount = 0;
        }
    }
}
