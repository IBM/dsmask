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

import java.io.File;

/**
 *
 * @author zinal
 */
public abstract class TestResources {

    protected File getSamplesDir() {
        return new File(".", "samples").getAbsoluteFile();
    }

}
