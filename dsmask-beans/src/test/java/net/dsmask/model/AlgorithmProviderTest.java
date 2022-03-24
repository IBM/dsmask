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
package net.dsmask.model;

import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author zinal
 */
public class AlgorithmProviderTest {

    @Test
    public void test1() throws Exception {
        AlgorithmInfo ai;

        ai = AlgorithmProvider.getInstance().findAlgorithm("FPE");
        Assert.assertNotNull("FPE algo exists", ai);

        ai = AlgorithmProvider.getInstance().findAlgorithm("LookupHash");
        Assert.assertNotNull("LookupHash algo exists", ai);
    }
}
