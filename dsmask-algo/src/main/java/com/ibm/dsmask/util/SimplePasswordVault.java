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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.binary.Base64;

/**
 * A very basic password vault for storing passwords in the encrypted form.
 * @author zinal
 */
public class SimplePasswordVault {

    private static final org.slf4j.Logger LOG =
            org.slf4j.LoggerFactory.getLogger(SimplePasswordVault.class);

    public static final String PROP_STORAGE = "com.ibm.dsmask.passwords";

    private static final String ENCRYPT_ALGO = "AES";
    private static final int AREA_SZ = 1024;
    private static final int KEY_SZ = 256 / 8;

    private final File file;
    private final SecretKey secretKey;
    private final Map<String, Entry> data;

    public SimplePasswordVault(File file) {
        this.file = file;
        this.secretKey = readOrGenerate(file);
        try {
            this.data = loadEntries(file, this.secretKey);
        } catch(Exception ex) {
            throw DsMaskUtil.toRE("Failed to load password storage", ex);
        }
        LOG.debug("Loaded password storage at {}", file);
    }

    public SimplePasswordVault() {
        this(getDefaultStorage());
    }

    public File getStorage() {
        return file;
    }

    public Entry getEntry(String key) {
        final Entry e = data.get(key);
        if (e==null)
            return e;
        return new Entry(e);
    }

    public void removeEntry(String key) {
        data.remove(key);
    }

    public void putEntry(String key, Entry entry) {
        if (entry==null)
            data.remove(key);
        else
            data.put(key, new Entry(entry));
    }

    public void putEntry(String key, String login, String password) {
        putEntry(key, new Entry(login, password));
    }

    public Entry makeEntry(String login, String password) {
        return new Entry(login, password);
    }

    public void save() {
        try {
            saveEntries(file, secretKey, data);
            LOG.debug("Saved password storage at {}", file);
        } catch(Exception ex) {
            throw DsMaskUtil.toRE("Error saving passwords", ex);
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

    public static SecretKey readOrGenerate(File fmain) {
        try {
            File fstash = new File(fmain.getPath() + ".stsh");
            if (fstash.exists()) {
                return readKeyFromStash(fstash);
            }
            SecretKey sks = generateKey();
            writeKeyToStash(fstash, sks);
            return sks;
        } catch(Exception ex) {
            throw DsMaskUtil.toRE("Cannot obtain password encryption key", ex);
        }
    }

    public static SecretKey generateKey() throws Exception {
        KeyGenerator generator = KeyGenerator.getInstance(ENCRYPT_ALGO);
        generator.init(256, new SecureRandom());
        return generator.generateKey();
    }

    /**
     * Stashing a secret key.
     * The idea is to make the key hard to grab by eyes.
     * @param fstash Stash file to store the secret key
     * @param sks Secret key itself
     * @throws Exception
     */
    public static void writeKeyToStash(File fstash, SecretKey sks)
            throws Exception {
        final byte[] data = sks.getEncoded();
        if (data.length != KEY_SZ)
            throw new IllegalStateException("Internal algorithm error 1");
        final SecureRandom rand = new SecureRandom();
        final byte[] ba1 = new byte[AREA_SZ];
        final byte[] ba2 = new byte[AREA_SZ];
        final byte[] ba3 = new byte[AREA_SZ];
        // Fill the two arrays with random data
        rand.nextBytes(ba1);
        rand.nextBytes(ba2);
        // Put the secret key into the second array at offset
        // defined by the first two (random) bytes of the second array.
        final int offset = 2 +
                ( ( (ba2[0] & 0xFF) + 256 * (ba2[1] & 0xFF) )
                    % (AREA_SZ - KEY_SZ - 2) );
        for (int pos = 0; pos < KEY_SZ; ++pos) {
            ba2[pos + offset] = data[pos];
        }
        // Compute the XOR product of the first and second arrays
        for (int pos = 0; pos < AREA_SZ; ++pos) {
            ba3[pos] = (byte)( (ba2[pos] & 0xFF) ^ (ba1[pos] & 0xFF) );
        }
        // Write the first array and the XOR product of the first and second.
        try (FileOutputStream fos = new FileOutputStream(fstash)) {
            fos.write(ba1);
            fos.write(ba3);
        }
    }

    /**
     * Unstashing the secret key from the input file.
     * @param fstash Stash file which stores the secret key
     * @return Secret key itself
     * @throws Exception
     */
    public static SecretKey readKeyFromStash(File fstash) throws Exception {
        final byte[] ba1 = new byte[AREA_SZ];
        final byte[] ba2 = new byte[AREA_SZ];
        final byte[] ba3 = new byte[AREA_SZ];
        try (FileInputStream fis = new FileInputStream(fstash)) {
            int count;
            count = fis.read(ba1);
            if (count!=AREA_SZ)
                throw new Exception("[1] Illegal read count: " + count + ", expected: " + AREA_SZ);
            count = fis.read(ba3);
            if (count!=AREA_SZ)
                throw new Exception("[2] Illegal read count: " + count + ", expected: " + AREA_SZ);
        }
        // Restore the second array as the the XOR product of the third and first arrays
        for (int pos = 0; pos < AREA_SZ; ++pos) {
            ba2[pos] = (byte)( (ba3[pos] & 0xFF) ^ (ba1[pos] & 0xFF) );
        }
        // Recover the key storage offset
        final int offset = 2 +
                ( ( (ba2[0] & 0xFF) + 256 * (ba2[1] & 0xFF) )
                    % (AREA_SZ - KEY_SZ - 2) );
        // Recover the secret key data
        final byte[] data = new byte[KEY_SZ];
        for (int pos = 0; pos < KEY_SZ; ++pos) {
            data[pos] = ba2[pos + offset];
        }
        return new SecretKeySpec(data, ENCRYPT_ALGO);
    }

    private static void saveEntries(File file, SecretKey sks,
            Map<String, Entry> m) throws Exception {
        // Copy the map for writing
        Map<String, Entry> work = new java.util.TreeMap<>(m);
        // Serialize the map
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(work);
        oos.flush();
        // Encrypt the serialized data
        final Cipher c = Cipher.getInstance(ENCRYPT_ALGO);
        c.init(Cipher.ENCRYPT_MODE, sks);
        final byte[] encryptedData = c.doFinal(baos.toByteArray());
        // Write the data to output file
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(encryptedData);
        }
    }

    private static Map<String, Entry> loadEntries(File file, SecretKey sks)
            throws Exception {
        if (! file.exists())
            return new HashMap<>();
        // Validate the file size
        final long fileSize = file.length();
        if (fileSize > 10*1024*1024) {
            throw new UnsupportedOperationException("Password files larger than 10M are not supported");
        }
        if (fileSize==0L)
            return new HashMap<>();
        // Read the input file
        final int bytes = (int) fileSize;
        final byte[] encryptedData = new byte[bytes];
        try (FileInputStream fis = new FileInputStream(file)) {
            int count = fis.read(encryptedData);
            if (count != encryptedData.length)
                throw new Exception("[3] Illegal read count: " + count + ", expected: " + encryptedData.length);
        }
        // Decrypt the data
        final Cipher c = Cipher.getInstance(ENCRYPT_ALGO);
        c.init(Cipher.DECRYPT_MODE, sks);
        final byte[] currentData = c.doFinal(encryptedData);
        // Deserialize the map
        final ByteArrayInputStream bais = new ByteArrayInputStream(currentData);
        final ObjectInputStream ois = new ObjectInputStream(bais);
        @SuppressWarnings("unchecked")
        Map<String, Entry> work = (Map<String, Entry>) ois.readObject();
        return new HashMap<>(work);
    }

    public static class Entry implements Serializable {
        private static final long serialVersionUID = 20220225001L;

        public String login;
        public String password;

        public Entry(String login, String password) {
            this.login = login;
            this.password = password;
        }

        public Entry(Entry e) {
            this.login = e.login;
            this.password = e.password;
        }
    }

}
