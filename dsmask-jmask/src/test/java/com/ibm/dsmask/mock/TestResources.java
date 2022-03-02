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
package com.ibm.dsmask.mock;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import com.ibm.dsmask.beans.MskContext;
import com.ibm.dsmask.beans.MskKey;
import com.ibm.dsmask.impl.XKeeper;
import com.ibm.dsmask.impl.XServices;
import net.dsmask.uniq.UniqProviderFactory;
import net.dsmask.uniq.UniqProvider;
import net.dsmask.uniq.UniqStore;

/**
 *
 * @author zinal
 */
public class TestResources {

    private MskContext context = null;
    private XKeeper keeper = null;
    private XServices services = null;

    protected MskContext getContext() {
        if (context==null) {
            context = new MskContext();
            File d = new File("..", "dict-data");
            d = new File(d, "ru");
            context.setDictPath(d.getAbsolutePath());
            context.addKey(new MskKey("default", "zztop-key"));
            context.addKey(new MskKey("qazwsx", "qazwsx"));
            context.addKey(new MskKey("dateop-test", "ieZahch4 Eepoa7ee ungam5Lu"));
        }
        return context;
    }

    protected XServices getServices() {
        if (services==null) {
            services = new XServices();
            services.setUniqProviderFactory(new UniqStoreGen());
        }
        return services;
    }

    protected XKeeper getKeeper() {
        if (keeper==null) {
            keeper = new XKeeper(getContext(), getServices());
        }
        return keeper;
    }

    protected static final class UniqStoreExt extends UniqStore {

        public UniqStoreExt() {
            super((File) null, 1, 100, 60);
        }

        @Override
        public void close() {
            // Dump the data for analysis
            File dir = new File(System.getProperty("java.io.tmpdir"),
                    "uniq-check_" + new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date()));
            dir.mkdir();
            System.out.println("Dumping UNIQ-CHECK data into " + dir);
            try {
                this.dump(dir);
                System.out.println("Dumping UNIQ-CHECK data finished.");
            } catch(Exception ex) {
                ex.printStackTrace(System.out);
            }
            // And finally, close the shards
            super.close();
        }

    }

    protected static final class UniqStoreGen implements UniqProviderFactory  {

        @Override
        public UniqProvider createProvider() {
            return new UniqStoreExt();
        }

    }

}
