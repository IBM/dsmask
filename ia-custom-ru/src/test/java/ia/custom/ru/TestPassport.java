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
public class TestPassport {

    @Test
    public void test() {
        final PassportDomestic pd = new PassportDomestic();
        final PassportForeign pf = new PassportForeign();

        String dom1 = "28 02 324411";
        boolean dom1pd = pd.matchValue(dom1);
        Assert.assertEquals(true, dom1pd);
        boolean dom1pf = pf.matchValue(dom1);
        Assert.assertEquals(false, dom1pf);

        String dom2 = "28 02 324411";
        boolean dom2pd = pd.matchValue(dom2);
        Assert.assertEquals(true, dom2pd);
        boolean dom2pf = pf.matchValue(dom2);
        Assert.assertEquals(false, dom2pf);

        String for1 = "69 3244112";
        boolean for1pd = pd.matchValue(for1);
        Assert.assertEquals(false, for1pd);
        boolean for1pf = pf.matchValue(for1);
        Assert.assertEquals(true, for1pf);

        String for2 = "34 1234567";
        boolean for2pd = pd.matchValue(for2);
        Assert.assertEquals(false, for2pd);
        boolean for2pf = pf.matchValue(for2);
        Assert.assertEquals(true, for2pf);
    }

}
