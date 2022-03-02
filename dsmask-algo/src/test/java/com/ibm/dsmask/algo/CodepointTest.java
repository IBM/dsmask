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
package com.ibm.dsmask.algo;

import org.junit.Assert;
import org.junit.Test;
import net.dsmask.util.DsMaskUtil;

/**
 *
 * @author zinal
 */
public class CodepointTest {

    @Test
    public void checkCodepoint02() {
        String v = DsMaskUtil.fromCodepoint(2);
        Assert.assertNotEquals("?", v);
        Assert.assertNotEquals(" ", v);
        Assert.assertEquals(1, v.length());
        Assert.assertEquals(2, (int) v.charAt(0));
    }

}
