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
package net.dsmask.demo;

import java.util.Arrays;
import net.dsmask.algo.MaskFPE;
import net.dsmask.algo.MaskFPH;
import org.apache.commons.text.RandomStringGenerator;

/**
 * Minimal performance test and demo for FPx implementations.
 * @author zinal
 */
public class FpxPerf {

    public static void main(String[] args) {
        System.out.println("FpxPerf: initializing...");
        final MaskFPE fpe = new MaskFPE();
        final MaskFPH fph = new MaskFPH();

        fpe.calculate("AAA");
        fph.calculate("AAA");

        long time0, time1;
        final String[] input;

        System.out.println("FpxPerf: generating input...");
        time0 = System.currentTimeMillis();
        input = generateInput();
        time1 = System.currentTimeMillis();
        System.out.println("FpxPerf: generated total "
                + String.valueOf(input.length)
                + " values in "
                + String.valueOf(time1 - time0)
                + " msec."
        );

        final String[] output = new String[input.length];

        System.out.println("FpxPerf: measuring FPE speed with iteration 0...");
        Arrays.fill(output, null);
        time0 = System.currentTimeMillis();
        for (int pos = 0; pos < input.length; ++pos) {
            output[pos] = fpe.calculate(input[pos]);
        }
        time1 = System.currentTimeMillis();
        printSpeed(time0, time1, input.length);

        System.out.println("FpxPerf: measuring FPE speed with iteration 1...");
        Arrays.fill(output, null);
        time0 = System.currentTimeMillis();
        for (int pos = 0; pos < input.length; ++pos) {
            output[pos] = fpe.calculate(input[pos], 1);
        }
        time1 = System.currentTimeMillis();
        printSpeed(time0, time1, input.length);

        System.out.println("FpxPerf: measuring FPH speed with iteration 0...");
        Arrays.fill(output, null);
        time0 = System.currentTimeMillis();
        for (int pos = 0; pos < input.length; ++pos) {
            output[pos] = fph.calculate(input[pos]);
        }
        time1 = System.currentTimeMillis();
        printSpeed(time0, time1, input.length);

        System.out.println("FpxPerf: measuring FPH speed with iteration 1...");
        Arrays.fill(output, null);
        time0 = System.currentTimeMillis();
        for (int pos = 0; pos < input.length; ++pos) {
            output[pos] = fph.calculate(input[pos], 1);
        }
        time1 = System.currentTimeMillis();
        printSpeed(time0, time1, input.length);
    }

    private static void printSpeed(long tv0, long tv1, int len) {
        final long time = tv1 - tv0;
        final double rate = 1000.0 * ((double)len) / ((double)time);
        System.out.println("\t " + String.valueOf(time) + " msec. = "
                + String.format("%.2f", rate) + " values/second."
        );
    }

    private static String[] generateInput() {
        char[][] pairs = {{'a', 'z'}, {'0', '9'}, {'A', 'Z'}, {'а', 'я'}, {' ', '/'}};
        final RandomStringGenerator rsg = new RandomStringGenerator.Builder()
                .withinRange(pairs).build();
        final String[] retval = new String[1000000];
        for (int pos = 0; pos < retval.length; ++pos)
            retval[pos] = rsg.generate(50 + (pos % 50));
        return retval;
    }

}
