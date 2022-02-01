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

import java.io.File;
import com.ibm.dsmask.jconf.impl.*;

/**
 *
 * @author zinal
 */
public class MaskingRuleRegistryMock {

    private static final org.slf4j.Logger LOG
            = org.slf4j.LoggerFactory.getLogger(MaskingRuleRegistryMock.class);

    private static MaskingRuleRegistry registry = null;

    public static MaskingRuleRegistry load() {
        if ( registry == null ) {
            try {
                File path = new File(new File(".."), "rules-testsuite");
                String strPath = path.getCanonicalPath();
                LOG.info("Rules directory: {}", strPath);
                registry = new MaskingRulesXmlReader().readBulk(strPath);
                LOG.info("Total rules loaded: {}", registry.getRulesCount());
            } catch(Exception ex) {
                throw new RuntimeException("MaskingRuleRegistryMock.populate()", ex);
            }
        }
        return registry;
    }

}
