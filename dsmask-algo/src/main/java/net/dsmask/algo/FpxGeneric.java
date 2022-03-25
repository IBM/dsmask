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
import net.dsmask.util.DsMaskUtil;

/**
 * Naive Java-only FPE algorithm implementation.
 * Hashing is configurable through the IndexGenFactory interface.
 * @author mzinal
 */
public class FpxGeneric {

    // + skip chars before and after
    private final int skipBefore;
    private final int skipAfter;
    // + allow returning the unmodified value
    private final boolean allowSameVal;
    // + character classes
    private final CharClassSet charClassSet;
    // + index generator
    private final FpxIndexFactory indexFactory;

    /**
     * Construct the masking algorithm instance.
     * @param cset Character set class definition
     * @param userKey User key string - if null or empty, default key will be used instead
     * @param skipBefore Number of characters to be skipped unmasked at the beginning
     * @param skipAfter Number of characters to be skipped unmasked at the end
     * @param allowSameVal Allow unmodified input
     * @param indexFactory Index factory to use
     */
    public FpxGeneric(CharClassSet cset, String userKey,
            int skipBefore, int skipAfter, boolean allowSameVal,
            FpxIndexFactory indexFactory) {
        this(cset,
                (userKey==null) ? (byte[])null : userKey.getBytes(StandardCharsets.UTF_8),
                skipBefore, skipAfter, allowSameVal, indexFactory);
    }

    /**
     * Construct the masking algorithm instance.
     * @param cset Character set class definition
     * @param userKey User key bytes - if null or empty, default key will be used instead
     * @param skipBefore Number of characters to be skipped unmasked at the beginning
     * @param skipAfter Number of characters to be skipped unmasked at the end
     * @param allowSameVal Allow unmodified input
     * @param indexFactory Index factory to use
     */
    public FpxGeneric(CharClassSet cset, byte[] userKey,
            int skipBefore, int skipAfter, boolean allowSameVal,
            FpxIndexFactory indexFactory) {
        this.charClassSet = cset;
        if (skipBefore < 0)
            skipBefore = 0;
        if (skipAfter < 0)
            skipAfter = 0;
        this.skipBefore = skipBefore;
        this.skipAfter = skipAfter;
        this.allowSameVal = allowSameVal;
        this.indexFactory = indexFactory;
    }

    public final int getSkipBefore() {
        return skipBefore;
    }

    public final int getSkipAfter() {
        return skipAfter;
    }

    public final CharClassSet getCharClassSet() {
        return charClassSet;
    }

    public final String calculate(Object in) {
        return calculate(in, 0);
    }

    public final String calculate(Object in, int iteration) {
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
            if (allowSameVal || !retval.equalsIgnoreCase(value))
                return retval;
            if ( ++substep > 1000 ) {
                throw new RuntimeException("Hanged FPE on input value [" + value + "]");
            }
        }
    }

    private String algo(String value, int iteration, int substep) {
        final int[] codePoints = value.codePoints().toArray();
        if (codePoints.length <= skipBefore + skipAfter)
            return value; // Nothing to mask, value is too short

        // prepare the current index generator object
        final String iterationData = ((iteration==0) && (substep==0)) ? null :
                (Integer.toHexString(iteration) + "." + Integer.toHexString(substep));
        final FpxIndexGen indexGen = indexFactory.make(value, iterationData);

        // output buffer
        final StringBuilder retval = new StringBuilder(value.length());
        // prepend with the skipped characters
        for ( int ix = 0; ix < skipBefore; ++ix ) {
            DsMaskUtil.appendCodepoint(retval, codePoints[ix]);
        }
        // generate the hash-based replacement
        final int lastIndex = codePoints.length - skipAfter;
        for ( int ix = skipBefore; ix < lastIndex; ++ix ) {
            int curCP = codePoints[ix];
            int toAppend = curCP;
            CharClassSet.Entry charClass = charClassSet.findClass(curCP);
            if (!charClass.isEmpty()) {
                int index = indexGen.nextIndex(charClass.getLength());
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
