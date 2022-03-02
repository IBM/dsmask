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
import java.util.BitSet;
import javax.crypto.Mac;

/**
 * Replacement index generator for an input value.
 * @author mzinal
 */
public final class IndexGenFPE {

    private final String value;
    private final Mac mac;
    private final int iteration;
    private byte[] macValue = null;
    private BitSet macBits = null;
    private int bitsPosition = 0;

    public IndexGenFPE(String value, Mac mac, int iteration) {
        this.value = value;
        this.mac = mac;
        this.iteration = iteration;
    }

    /**
     * Return a MAC value for input string, computing (and caching)
     * it as necessary
     * @return MAC value for an input string
     */
    public byte[] getMacValue() {
        if (macValue == null) {
            try {
                if (iteration > 0) {
                    mac.update(Integer.toHexString(iteration)
                            .getBytes(StandardCharsets.UTF_8));
                    mac.update((byte)1);
                }
                macValue = mac.doFinal(value.getBytes(StandardCharsets.UTF_8));
            } catch (Exception ex) {
                // MAC object cleanup for the worst case
                try {
                    mac.doFinal();
                } catch (Exception tmp) {
                }
                throw new RuntimeException("Failed to compute MAC", ex);
            }
        }
        return macValue;
    }

    /**
     * Retrieve next bit from a MAC value, recycling the MAC as necessary
     * @return true, if bit is 1, false otherwise
     */
    public boolean getNextBit() {
        if (macBits == null || bitsPosition >= macBits.length()) {
            macBits = BitSet.valueOf(getMacValue());
            bitsPosition = 0;
        }
        return macBits.get(bitsPosition++);
    }

    /**
     * Retrieve a MAC-based index in an array of specified size
     * @param size Size of an array
     * @return Index value from 0 to size-1
     */
    public int getNextIndex(int size) {
        if (size < 2) {
            return 0;
        }
        // Compute the number of bits required
        int maxVal = 1;
        int bitCount = 0;
        while (maxVal <= size) {
            bitCount += 1;
            maxVal *= 2;
        }
        // Generate the index of desired size based on MAC bits.
        int index = 0;
        maxVal = 1;
        for (int pos = 0; pos < bitCount; ++pos) {
            if (getNextBit()) {
                index += maxVal;
            }
            maxVal *= 2;
        }
        return index % size;
    }

}
