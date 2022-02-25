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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.binary.Base64;
import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;

/**
 * A very basic password vault for storing passwords in the encrypted form.
 * @author zinal
 */
public class SimplePasswordVault implements AutoCloseable {

    private static final org.slf4j.Logger LOG =
            org.slf4j.LoggerFactory.getLogger(SimplePasswordVault.class);

    public static final String PROP_STORAGE = "com.ibm.dsmask.passwords";
    public static final String ENCRYPT_ALGO = "AES";

    private final File file;
    private final MVStore store;
    private final MVMap<String,String> data;
    private final SecretKeySpec encryptionKey;

    public SimplePasswordVault(File file, boolean readonly) {
        this.file = file;
        final MVStore.Builder builder = new MVStore.Builder();
        builder.fileName(file.getAbsolutePath());
        if (readonly)
            builder.readOnly();
        this.store = builder.open();
        this.data = this.store.openMap("data");
        this.encryptionKey = readOrGenerate(this.store);
        LOG.debug("Opened password storage at {}", this.store.getFileStore().getFileName());
    }

    public SimplePasswordVault(boolean readonly) {
        this(getDefaultStorage(), readonly);
    }

    public SimplePasswordVault() {
        this(getDefaultStorage(), false);
    }

    public File getStorage() {
        return file;
    }

    public Entry getEntry(String key) {
        String vs = data.get(key);
        if (vs==null)
            return null;
        if (!Base64.isBase64(vs)) {
            LOG.warn("Corrupt entry on key {} - NOT_B64", key);
            return null;
        }
        byte[] vb = Base64.decodeBase64(vs);
        if (vb.length < 1) {
            LOG.warn("Corrupt entry on key {} - EMPTY", key);
            return null;
        }
        try {
            vb = decrypt(vb);
        } catch(Exception ex) {
            LOG.warn("Corrupt entry on key {} - DECRYPT", key, ex);
            return null;
        }

        int pos = indexOf(vb, (byte)0);
        if (pos < 0) {
            LOG.warn("Corrupt entry on key {} - NO_PAIR", key);
            return null;
        }

        String login = new String(vb, 0, pos, StandardCharsets.UTF_8);
        String password = new String(vb, pos+1, vb.length - (pos+1), StandardCharsets.UTF_8);
        return new Entry(login, password);
    }

    public void putEntry(String key, Entry entry) {
        putEntry(key, entry.login, entry.password);
    }

    public void putEntry(String key, String login, String password) {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            baos.write(login.getBytes(StandardCharsets.UTF_8));
            baos.write(0);
            baos.write(password.getBytes(StandardCharsets.UTF_8));
        } catch(Throwable ix) {
            throw new RuntimeException("Cannot serialize password entry: " + ix.getMessage(), ix);
        }
        final String value;
        try {
            final byte[] source = baos.toByteArray();
            final byte[] target = encrypt(source);
            value = Base64.encodeBase64String(target);
        } catch(Throwable ex) {
            throw new RuntimeException("Cannot encode password entry: " + ex.getMessage(), ex);
        }
        data.put(key, value);
    }

    public Entry makeEntry(String login, String password) {
        return new Entry(login, password);
    }

    private static int indexOf(byte[] a, byte v) {
        for (int pos=0; pos<a.length; ++pos) {
            if (v==a[pos])
                return pos;
        }
        return -1;
    }

    private byte[] encrypt(byte[] source) throws Exception {
        final Cipher c = Cipher.getInstance(ENCRYPT_ALGO);
        c.init(Cipher.ENCRYPT_MODE, encryptionKey);
        return c.doFinal(source);
    }

    private byte[] decrypt(byte[] source) throws Exception {
        final Cipher c = Cipher.getInstance(ENCRYPT_ALGO);
        c.init(Cipher.DECRYPT_MODE, encryptionKey);
        return c.doFinal(source);
    }

    @Override
    public void close() throws Exception {
        if (! store.isClosed()) {
            store.close();
        }
    }

    public static File getDefaultStorage() {
        String v = System.getProperty(PROP_STORAGE);
        if (v != null)
            v = v.trim();
        if (v!=null && v.length()>0)
            return new File(v);
        v = System.getProperty("user.home");
        if (v==null)
            throw new RuntimeException("User home cannot be determined for password vault");
        return new File(v, "dsmask-passwords");
    }

    public static SecretKeySpec readOrGenerate(MVStore store) {
        final MVMap<String,String> config = store.openMap("config");
        final String key = "system";
        String value = config.get(key);
        if (value == null) {
            final byte[] encKey = new byte[32];
            new SecureRandom().nextBytes(encKey);
            value = Base64.encodeBase64String(encKey);
            config.put(key, value);
            return new SecretKeySpec(encKey, ENCRYPT_ALGO);
        } else {
            return new SecretKeySpec(Base64.decodeBase64(value), ENCRYPT_ALGO);
        }
    }

    public static class Entry {
        public final String login;
        public final String password;

        public Entry(String login, String password) {
            this.login = login;
            this.password = password;
        }
    }

}
