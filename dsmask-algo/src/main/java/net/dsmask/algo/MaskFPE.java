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
package net.dsmask.algo;

import java.nio.charset.StandardCharsets;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import net.dsmask.util.DsMaskUtil;

/**
 * Naive Java-only FPE algorithm implementation.
 * @author mzinal
 */
public class MaskFPE {

    public static final String HMAC_NAME = "HmacSHA512";
    public static final String DEFAULT_KEY = "ваттерпежек0змА";

    // + skip chars before and after
    private final int skipBefore;
    private final int skipAfter;
    // + allow returning the unmodified value
    private final boolean allowSameVal;
    // + character classes
    private final CharClassSet charClassSet;
    // + key
    private final byte[] userKey;
    // + MAC object
    private final Mac mac;

    public MaskFPE() {
        this(CharClassSet.DEFAULT_RUSSIAN);
    }

    public MaskFPE(CharClassSet cset) {
        this(cset, "");
    }

    public MaskFPE(CharClassSet cset, String userKey) {
        this(cset, userKey, 0, 0, false);
    }

    public MaskFPE(CharClassSet cset, String userKey,
            int skipBefore, int skipAfter) {
        this(cset, userKey, skipBefore, skipAfter, false);
    }

    public MaskFPE(CharClassSet cset, String userKey,
            int skipBefore, int skipAfter, boolean allowSameVal) {
        this(cset,
                (userKey==null) ? (byte[])null : userKey.getBytes(StandardCharsets.UTF_8),
                skipBefore, skipAfter, allowSameVal);
    }

    public MaskFPE(CharClassSet cset, byte[] userKey,
            int skipBefore, int skipAfter, boolean allowSameVal) {
        this.charClassSet = cset;
        if (userKey==null || userKey.length==0)
            userKey = DEFAULT_KEY.getBytes(StandardCharsets.UTF_8);
        this.userKey = userKey;
        try {
            SecretKeySpec keySpec = new SecretKeySpec(userKey, HMAC_NAME);
            this.mac = Mac.getInstance(HMAC_NAME);
            this.mac.init(keySpec);
        } catch(Exception ex) {
            throw new RuntimeException("Cannot initialize MAC " + HMAC_NAME, ex);
        }
        if (skipBefore < 0)
            skipBefore = 0;
        if (skipAfter < 0)
            skipAfter = 0;
        this.skipBefore = skipBefore;
        this.skipAfter = skipAfter;
        this.allowSameVal = allowSameVal;
    }

    public int getSkipBefore() {
        return skipBefore;
    }

    public int getSkipAfter() {
        return skipAfter;
    }

    public CharClassSet getCharClassSet() {
        return charClassSet;
    }

    public byte[] getUserKey() {
        return userKey;
    }

    public String calculate(Object in) {
        return calculate(in, 0);
    }

    public String calculate(Object in, int iteration) {
        if (in==null)
            return null;
        String value = in.toString();
        if (value.length()==0)
            return value; // Empty string on input, same object on output
        if (value.length() <= (skipBefore + skipAfter)) {
            if (allowSameVal)
                return value;
            throw new RuntimeException("Input value [" + value + "] too short, "
                    + "needs to have at least "
                    + String.valueOf(skipBefore + skipAfter) + " characters");
        }
        // Protect against equal input and output
        int substep = 0;
        while (true) {
            String retval = algo(value, iteration, substep);
            if (! retval.equalsIgnoreCase(value))
                return retval;
            if (allowSameVal)
                return retval;
            if ( ++substep > 1000 ) {
                throw new RuntimeException("Hanged FPE on input value [" + value + "]");
            }
        }
    }

    private String algo(String value, int iteration, int substep) {
        int[] codePoints = value.codePoints().toArray();
        if (codePoints.length <= skipBefore + skipAfter)
            return value; // Nothing to mask, value is too short

        int lastIndex = codePoints.length - skipAfter;

        final String indexData = (substep==0) ? value :
                ( value + "." + Integer.toHexString(substep) );
        final IndexGenFPE ig = new IndexGenFPE(indexData, mac, iteration);
        final StringBuilder retval = new StringBuilder();

        // prepend with the skipped characters
        for ( int ix = 0; ix < skipBefore; ++ix ) {
            DsMaskUtil.appendCodepoint(retval, codePoints[ix]);
        }
        // generate the hash-based replacement
        for ( int ix = skipBefore; ix < lastIndex; ++ix ) {
            int curCP = codePoints[ix];
            int toAppend = curCP;
            CharClassSet.Entry charClass = charClassSet.findClass(curCP);
            if (!charClass.isEmpty()) {
                int index = ig.getNextIndex(charClass.getLength());
                toAppend = charClass.getCodePoint(index);
            }
            DsMaskUtil.appendCodepoint(retval, toAppend);
        }
        // append with the skipped characters
        for ( int ix = lastIndex; ix < codePoints.length; ++ix ) {
            DsMaskUtil.appendCodepoint(retval, codePoints[ix]);
        }
        return retval.toString();
    }


}
