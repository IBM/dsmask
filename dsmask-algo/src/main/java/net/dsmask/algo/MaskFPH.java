/*
 * Copyright (c) IBM Corp. 2018, 2022.
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
package net.dsmask.algo;

import java.nio.charset.StandardCharsets;

/**
 * Naive Java-only FPE algorithm implementation.
 * @author mzinal
 */
public class MaskFPH extends FpxGeneric {

    public static final String DEFAULT_KEY = "иди0синкразия";

    /**
     * Minimal constructor.
     */
    public MaskFPH() {
        this(CharClassSet.DEFAULT_RUSSIAN);
    }

    /**
     * Helper constructor.
     * @param cset Character set class definition
     */
    public MaskFPH(CharClassSet cset) {
        this(cset, "");
    }

    /**
     * Helper constructor.
     * @param cset Character set class definition
     * @param userKey User key string - if null or empty, default key will be used instead
     */
    public MaskFPH(CharClassSet cset, String userKey) {
        this(cset, userKey, 0, 0, false);
    }

    /**
     * Helper constructor.
     * @param cset Character set class definition
     * @param userKey User key string - if null or empty, default key will be used instead
     * @param skipBefore Number of characters to be skipped unmasked at the beginning
     * @param skipAfter Number of characters to be skipped unmasked at the end
     */
    public MaskFPH(CharClassSet cset, String userKey,
            int skipBefore, int skipAfter) {
        this(cset, userKey, skipBefore, skipAfter, false);
    }

    /**
     * Helper constructor.
     * @param cset Character set class definition
     * @param userKey User key string - if null or empty, default key will be used instead
     * @param skipBefore Number of characters to be skipped unmasked at the beginning
     * @param skipAfter Number of characters to be skipped unmasked at the end
     * @param allowSameVal Allow unmodified input
     */
    public MaskFPH(CharClassSet cset, String userKey,
            int skipBefore, int skipAfter, boolean allowSameVal) {
        this(cset,
                (userKey==null) ? (byte[])null : userKey.getBytes(StandardCharsets.UTF_8),
                skipBefore, skipAfter, allowSameVal);
    }

    /**
     * Real constructor implementation.
     * @param cset Character set class definition
     * @param userKey User key bytes - if null or empty, default key will be used instead
     * @param skipBefore Number of characters to be skipped unmasked at the beginning
     * @param skipAfter Number of characters to be skipped unmasked at the end
     * @param allowSameVal Allow unmodified input
     */
    public MaskFPH(CharClassSet cset, byte[] userKey,
            int skipBefore, int skipAfter, boolean allowSameVal) {
        super(cset, userKey, skipBefore, skipAfter, allowSameVal, 
                new FpxIndexHash(fixKey(userKey)));
    }

    public static byte[] fixKey(byte[] userKey) {
        if (userKey==null || userKey.length==0)
            userKey = DEFAULT_KEY.getBytes(StandardCharsets.UTF_8);
        return userKey;
    }

}
