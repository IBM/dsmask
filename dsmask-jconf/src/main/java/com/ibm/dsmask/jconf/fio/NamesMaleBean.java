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
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.text.StringTokenizer;
import com.ibm.dsmask.jconf.beans.Utils;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.text.WordUtils;

/**
 *
 * @author zinal
 */
public class NamesMaleBean extends NamesBean {

    private static final org.slf4j.Logger LOG
            = org.slf4j.LoggerFactory.getLogger(NamesMaleBean.class);

    private String midMale;
    private String midFemale;

    public NamesMaleBean() {
    }

    public NamesMaleBean(String name) {
        super(name);
    }

    public NamesMaleBean(String name, String midMale, String midFemale) {
        super(name);
        this.midMale = WordUtils.capitalizeFully(midMale);
        this.midFemale = WordUtils.capitalizeFully(midFemale);
    }

    public String getMidMale() {
        return midMale;
    }

    public void setMidMale(String midMale) {
        this.midMale = WordUtils.capitalizeFully(midMale);
    }

    public String getMidFemale() {
        return midFemale;
    }

    public void setMidFemale(String midFemale) {
        this.midFemale = WordUtils.capitalizeFully(midFemale);
    }


    public static final List<NamesMaleBean> readExtended(String fname)
            throws Exception {
        if (fname.startsWith("~/"))
            fname = System.getProperty("user.home") + fname.substring(1);
        try (FileInputStream fis = new FileInputStream(fname)) {
            return readExtended(fis);
        }
    }

    public static final List<NamesMaleBean> readExtended(InputStream is)
            throws Exception {
        final BufferedReader br =
                new BufferedReader(new InputStreamReader(is, "UTF-8"));
        final StringTokenizer stok = new StringTokenizer();
        final Map<String, NamesMaleBean> retval = new HashMap<>();
        String line;
        while ((line=br.readLine())!=null) {
            stok.reset(line);
            String[] items = stok.getTokenArray();
            if (items.length < 4)
                continue;
            String name = Utils.unquote(items[1]);
            if (name.length()==0)
                continue;
            String midMale = Utils.unquote(items[2]);
            String midFemale = Utils.unquote(items[3]);
            NamesMaleBean bean = new NamesMaleBean(name, midMale, midFemale);
            bean = retval.put(bean.getName(), bean);
            if (bean!=null) {
                LOG.warn("Duplicate input value [{}]", bean.getName());
            }
        }
        return new ArrayList<>(retval.values());
    }
}
