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
package ia.custom.ru;

import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author zinal
 */
public class TestOGRN {

    @Test
    public void test15() {
        String in1 = "319631300041247";
        boolean res1 = new OGRN().matchValue(in1);
        Assert.assertEquals(true, res1);
        String in2 = "319631041247";
        boolean res2 = new OGRN().matchValue(in2);
        Assert.assertEquals(false, res2);
        String in3 = "319631300041241";
        boolean res3 = new OGRN().matchValue(in3);
        Assert.assertEquals(false, res3);
    }

    @Test
    public void test13() {
        String in1 = "1027739004600";
        boolean res1 = new OGRN().matchValue(in1);
        Assert.assertEquals(true, res1);
        String in2 = "10277390046";
        boolean res2 = new OGRN().matchValue(in2);
        Assert.assertEquals(false, res2);
        String in3 = "1027739004601";
        boolean res3 = new TaxPayerId().matchValue(in3);
        Assert.assertEquals(false, res3);
    }

}
