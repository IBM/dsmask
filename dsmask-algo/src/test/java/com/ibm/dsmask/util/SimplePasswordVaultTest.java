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
package com.ibm.dsmask.util;

import java.io.File;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author zinal
 */
public class SimplePasswordVaultTest {

    @Test
    public void test() throws Exception {
        File temp = File.createTempFile("dsmask-algo-passw", "-test");
        temp.delete();
        try {
            String key1 = "key1", login1 = "ivanoff", password1 = "password1";
            String key2 = "key2", login2 = "petroff", password2 = "password2";
            String key3 = "key3", login3 = "sidoroff", password3 = "password3";
            String key4 = "key4", login4 = "zotoFF", password4 = "";
            String key5 = "key5", login5 = "", password5 = "";
            do {
                SimplePasswordVault spv = new SimplePasswordVault(temp);
                spv.putEntry(key1, login1, password1);
                spv.putEntry(key2, login2, password2);
                spv.putEntry(key3, login2, password2);
                spv.save();
                spv.putEntry(key3, login3, password3);
                spv.putEntry(key4, login4, password4);
                spv.putEntry(key5, login5, password5);
                spv.save();

                SimplePasswordVault.Entry e1 = spv.getEntry(key1);
                SimplePasswordVault.Entry e2 = spv.getEntry(key2);
                SimplePasswordVault.Entry e3 = spv.getEntry(key3);
                SimplePasswordVault.Entry e4 = spv.getEntry(key4);
                SimplePasswordVault.Entry e5 = spv.getEntry(key5);

                Assert.assertNotNull(e1);
                Assert.assertNotNull(e2);
                Assert.assertNotNull(e3);
                Assert.assertNotNull(e4);
                Assert.assertNotNull(e5);

                Assert.assertEquals(login1, e1.login);
                Assert.assertEquals(login2, e2.login);
                Assert.assertEquals(login3, e3.login);
                Assert.assertEquals(login4, e4.login);
                Assert.assertEquals(login5, e5.login);

                Assert.assertEquals(password1, e1.password);
                Assert.assertEquals(password2, e2.password);
                Assert.assertEquals(password3, e3.password);
                Assert.assertEquals(password4, e4.password);
                Assert.assertEquals(password5, e5.password);
            } while (false);
            do {
                SimplePasswordVault spv = new SimplePasswordVault(temp);
                SimplePasswordVault.Entry e1 = spv.getEntry(key1);
                SimplePasswordVault.Entry e2 = spv.getEntry(key2);
                SimplePasswordVault.Entry e3 = spv.getEntry(key3);

                Assert.assertNotNull(e1);
                Assert.assertNotNull(e2);
                Assert.assertNotNull(e3);

                Assert.assertEquals(login1, e1.login);
                Assert.assertEquals(login2, e2.login);
                Assert.assertEquals(login3, e3.login);

                Assert.assertEquals(password1, e1.password);
                Assert.assertEquals(password2, e2.password);
                Assert.assertEquals(password3, e3.password);
            } while (false);
        } finally {
            //DsMaskUtil.deleteFiles(temp);
        }
    }

}
