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
package net.dsmask.beans.xml;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipInputStream;

/**
 * ZipInputStream wrapper to avoid close() calls when reading
 * XML, properties and other similar data from a ZIP entry.
 * @author zinal
 */
public class InputStreamWrapper extends InputStream {

    private final ZipInputStream zis;

    public InputStreamWrapper(ZipInputStream zis) {
        this.zis = zis;
    }

    @Override
    public void close() throws IOException {
        zis.closeEntry();
    }

    @Override
    public int read() throws IOException {
        return zis.read();
    }

    @Override
    public boolean markSupported() {
        return zis.markSupported();
    }

    @Override
    public synchronized void reset() throws IOException {
        zis.reset();
    }

    @Override
    public synchronized void mark(int readlimit) {
        zis.mark(readlimit);
    }

    @Override
    public int available() throws IOException {
        return zis.available();
    }

    @Override
    public long skip(long n) throws IOException {
        return zis.skip(n);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        return zis.read(b, off, len);
    }

    @Override
    public int read(byte[] b) throws IOException {
        return zis.read(b);
    }

}
