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

import org.junit.Assert;
import org.junit.Test;
import com.ibm.dsmask.algo.CharClassSet;
import com.ibm.dsmask.algo.MaskFPE;

/**
 *
 * @author zinal
 */
public class UniqStoreTest {

    private final MaskFPE masker = new MaskFPE(CharClassSet.DEFAULT_RUSSIAN, "qazwsx");

    private static final String[] VALUES = {
        "0Xy", "3Rz",    // -> 0Bh
        "4VH", "0DH",    // -> 1CB
        "9sH", "3dR",    // -> 3eM
        "9LZ", "8TU"     // -> 1CB
    };

    @Test
    public void test() {
        try (UniqStore store = new UniqStore((java.io.File) null, 1, 20, 60)) {

            UniqRequest[] input1 = new UniqRequest[VALUES.length];
            for (int i=0; i<input1.length; ++i) {
                input1[i] = new UniqRequest();
                input1[i].setIteration(0);
                input1[i].setSource(new Object[] { VALUES[i] });
                input1[i].setTarget(new Object[] { masker.calculate(VALUES[i]) });
            }

            UniqResponse[] output1 = store.store("test", input1);
            for (int i=0; i<output1.length; ++i) {
                if ( i % 2 == 0 ) {
                    Assert.assertEquals(true, output1[i].isLinkedCorrectly());
                } else {
                    Assert.assertEquals(false, output1[i].isLinkedCorrectly());
                }
            }

            UniqRequest[] input2 = new UniqRequest[VALUES.length / 2];
            for (int i=0; i<input2.length; ++i) {
                int srcIndex = (2*i)+1;
                input2[i] = new UniqRequest();
                input2[i].setIteration(1);
                input2[i].setSource(new Object[] { VALUES[srcIndex] });
                input2[i].setTarget(new Object[] { masker.calculate(VALUES[srcIndex], 1) });
            }

            UniqResponse[] output2 = store.store("test", input2);
            for (int i=0; i<output2.length; ++i) {
                Assert.assertEquals(true, output2[i].isLinkedCorrectly());
            }

        }
    }

}
