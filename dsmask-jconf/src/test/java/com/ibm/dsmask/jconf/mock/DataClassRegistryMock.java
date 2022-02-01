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
package com.ibm.dsmask.jconf.mock;

import com.ibm.dsmask.jconf.beans.*;
import com.ibm.dsmask.jconf.impl.*;
import com.ibm.dsmask.mock.MockDCS;

/**
 *
 * @author zinal
 */
public class DataClassRegistryMock extends DataClassRegistry {
    
    /**
     * Load the values from the mock set of data classes
     */
    @Override
    public void polulate() {
        for (MockDCS kdc : MockDCS.values()) {
            add(new DataClass(kdc.name, kdc.mode));
        }
    }
    
}
