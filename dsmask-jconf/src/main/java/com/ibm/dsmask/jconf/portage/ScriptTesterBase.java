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
package com.ibm.dsmask.jconf.portage;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import com.ibm.dsmask.util.ByteArrayWrapper;
import com.ibm.dsmask.jconf.beans.*;

/**
 * Common stuff for script testers.
 * Currently includes just log suppression logic for script logging API.
 * Similar to @com.ibm.dsmask.impl.ScriptRunnerBase
 * @author zinal
 */
public abstract class ScriptTesterBase {

    private static final org.slf4j.Logger LOG
            = org.slf4j.LoggerFactory.getLogger(ScriptTesterBase.class);

    // message logging limiter
    private final Map<String, MessageInfo> messageInfo = new HashMap<>();
    // digest for message uniqueness
    private final MessageDigest digest;

    public ScriptTesterBase() {
        try {
            this.digest = MessageDigest.getInstance("SHA-512");
        } catch(Exception ex) {
            throw new RuntimeException("Failed to initialize SHA-512", ex);
        }
    }

    public abstract boolean test(String text, String input, boolean predicate);

    public boolean test(MaskingFunction mf) {
        return test(mf.getText(), mf.getTextInput(), false);
    }
    
    public boolean test(MaskingPredicate mp) {
        return test(mp.getText(), mp.getInput(), true);
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
        sb.append("script-tester ");
        sb.append("[").append(msgType).append("] ").append(msgText);
        if (++mi.currentCount >= mi.maxCount) {
            sb.append("\n").append("[").append(msgType)
                    .append("] ** Further messages suppressed");
        }
        LOG.info("{}", sb);
        return true;
    }

    public boolean logOnce(String msgType, String msgText) {
        return logMessage(msgType, 1, msgText);
    }
    
    public void dumpException(Throwable ex) {
        if (ex==null)
            return;
        final String eol = System.lineSeparator();
        final StringBuilder sb = new StringBuilder();
        while (ex != null) {
            sb.append('\t');
            sb.append(ex.getClass().getName());
            sb.append(" ");
            sb.append(ex.getMessage());
            sb.append(eol);
            for (StackTraceElement ste : ex.getStackTrace()) {
                sb.append("\t\t");
                sb.append(ste.getClassName());
                sb.append(" : ");
                sb.append(ste.getLineNumber());
                sb.append(" / ");
                sb.append(ste.getMethodName());
                sb.append(eol);
            }
            ex = ex.getCause();
        }
        LOG.warn("{}", sb);
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
