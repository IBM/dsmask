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
package com.ibm.dsmask;

import org.junit.Test;
import static org.junit.Assert.*;
import com.ibm.dsmask.mock.*;

/**
 *
 * @author zinal
 */
public class DsMaskOneTest extends TestResources {

    public DsMaskOneTest() {
    }

    @Test
    public void test() throws Exception {
        try (DsConfigMock configuration = new DsConfigMock(MockData.TAB1)) {
            DsMask operator = new DsMask();
            operator.setServices(getServices());
            operator.validateConfiguration(configuration, true);
            operator.initialize();
            operator.process();
        }
        assertEquals(true, true);
    }

}
