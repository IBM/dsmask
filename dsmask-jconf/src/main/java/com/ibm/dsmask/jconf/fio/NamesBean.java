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
package com.ibm.dsmask.jconf.fio;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.text.WordUtils;
import org.apache.commons.text.StringTokenizer;
import com.ibm.dsmask.jconf.beans.Utils;

/**
 *
 * @author zinal
 */
public class NamesBean {

    private static final org.slf4j.Logger LOG
            = org.slf4j.LoggerFactory.getLogger(NamesBean.class);

    private String name;
    private byte[] sortKey;

    public NamesBean() {
    }

    public NamesBean(String name) {
        this.name = WordUtils.capitalizeFully(name);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = WordUtils.capitalizeFully(name);
    }

    public byte[] getSortKey(Charset cset) {
        if (sortKey==null)
            sortKey = name.getBytes(cset);
        return sortKey;
    }

    public static final List<NamesBean> readSimple(String fname)
            throws Exception {
        if (fname.startsWith("~/"))
            fname = System.getProperty("user.home") + fname.substring(1);
        try (FileInputStream fis = new FileInputStream(fname)) {
            return readSimple(fis);
        }
    }

    public static final List<NamesBean> readSimple(InputStream is)
            throws Exception {
        final BufferedReader br =
                new BufferedReader(new InputStreamReader(is, "UTF-8"));
        final StringTokenizer stok = new StringTokenizer();
        final Map<String, NamesBean> retval = new HashMap<>();
        String line;
        while ((line=br.readLine())!=null) {
            stok.reset(line);
            String[] items = stok.getTokenArray();
            if (items.length < 2)
                continue;
            String value = Utils.unquote(items[1]);
            if (value.length()==0)
                continue;
            NamesBean bean = new NamesBean(value);
            bean = retval.put(bean.getName(), bean);
            if (bean!=null) {
                LOG.warn("Duplicate input value [{}]", bean.getName());
            }
        }
        return new ArrayList<>(retval.values());
    }

}
