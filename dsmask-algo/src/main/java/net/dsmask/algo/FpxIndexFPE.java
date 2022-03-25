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
import java.util.BitSet;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * Replacement index generator, FPE implementation.
 * @author zinal
 */
public class FpxIndexFPE implements FpxIndexFactory {

    private final Mac mac;

    public FpxIndexFPE(byte[] userKey, String hmacName) {
        try {
            SecretKeySpec keySpec = new SecretKeySpec(userKey, hmacName);
            this.mac = Mac.getInstance(hmacName);
            this.mac.init(keySpec);
        } catch(Exception ex) {
            throw new RuntimeException("Cannot initialize MAC " + hmacName, ex);
        }
    }

    @Override
    public FpxIndexGen make(String value, String iteration) {
        return new IndexGen(getMacValue(value, iteration));
    }

    public final byte[] getMacValue(String value, String iteration) {
        try {
            if (iteration != null && iteration.length() > 0) {
                mac.update(iteration.getBytes(StandardCharsets.UTF_8));
                mac.update((byte)1);
            }
            return mac.doFinal(value.getBytes(StandardCharsets.UTF_8));
        } catch (Exception ex) {
            // MAC object cleanup for the worst case
            try {
                mac.doFinal();
            } catch (Exception tmp) {
            }
            throw new RuntimeException("Failed to compute MAC", ex);
        }
    }

    public static class IndexGen implements FpxIndexGen {

        private final byte[] macValue;
        private BitSet macBits = null;
        private int bitsPosition = 0;

        public IndexGen(byte[] macValue) {
            this.macValue = macValue;
        }

        /**
         * Retrieve next bit from a MAC value, recycling the MAC as necessary
         * @return true, if bit is 1, false otherwise
         */
        public boolean getNextBit() {
            if (macBits == null || bitsPosition >= macBits.length()) {
                macBits = BitSet.valueOf(macValue);
                bitsPosition = 0;
            }
            return macBits.get(bitsPosition++);
        }

        @Override
        public int nextIndex(int charCount) {
            if (charCount < 2) {
                return 0;
            }
            // Compute the number of bits required
            int maxVal = 1;
            int bitCount = 0;
            while (maxVal <= charCount) {
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
            return index % charCount;
        }

    }

}
