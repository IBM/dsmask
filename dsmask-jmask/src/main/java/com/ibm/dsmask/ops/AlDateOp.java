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
package com.ibm.dsmask.ops;

import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import com.ibm.dsmask.impl.*;
import com.ibm.dsmask.beans.MskFunc;
import com.ibm.dsmask.algo.PureJavaCrc32;

/**
 * Date hash-based consistent masking.
 * Preserves the year number, does not modify the time inside the day.
 * @author zinal
 */
public class AlDateOp implements AlSimpleValue {

    private final String userKey;
    private final DateFormat dateFormat;
    private final DateFormat customFormat;
    private byte[] userKeyBytes = null;

    public AlDateOp(XKeeper ctx, MskFunc function) {
        List<String[]> ops = Utils.parseConfig(function.getText());
        this.userKey = ctx.getUserKey(Utils.getConfigValue(ops, "key"));
        String dateFormatText = "yyyy-MM-dd";
        if (Utils.hasConfigEntry(ops, "format"))
            dateFormatText = Utils.getConfigValue(ops, "format");
        this.dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        this.customFormat = new SimpleDateFormat(dateFormatText);
    }

    @Override
    public Object exec(Object in) {
        if (in==null)
            return null;
        final FieldFormat format;
        final java.util.Date source;
        if (in instanceof java.sql.Date) {
            source = (java.util.Date) in;
            format = FieldFormat.SqlDate;
        } else if (in instanceof java.sql.Timestamp) {
            source = (java.util.Date) in;
            format = FieldFormat.SqlTimestamp;
        } else {
            String tmp = in.toString();
            if (tmp.length()==0)
                return null;
            try {
                source = customFormat.parse(tmp);
            } catch(ParseException px) {
                // for invalid dates, return some arbitrary date in the past
                // TODO: log warning
                final long stamp = System.currentTimeMillis() -
                        ThreadLocalRandom.current()
                                .nextLong(31536000000L, 315360000000L);
                return customFormat.format(new java.util.Date(stamp));
            }
            format = FieldFormat.String;
        }
        final Calendar cal = Calendar.getInstance();
        cal.setTime(source);
        // Protect against returning the very same date.
        final int oldDays = cal.get(Calendar.DAY_OF_YEAR) - 1;
        int newDays, substep = 0;
        do {
            newDays = getHashDays(source, substep);
            if (++substep > 10000) {
                throw new RuntimeException("Hang on date value " + source);
            }
        } while(oldDays == newDays);
        // Keep the same year, changing just the date within
        cal.set(cal.get(Calendar.YEAR), 0, 1);
        cal.add(Calendar.DAY_OF_YEAR, newDays);
        // Same data type on output
        switch (format) {
            case SqlDate:
                return new java.sql.Date(cal.getTimeInMillis());
            case SqlTimestamp:
                return new java.sql.Timestamp(cal.getTimeInMillis());
            case String:
                return customFormat.format(cal.getTime());
        }
        throw new IllegalStateException(); // unreached
    }

    public int getHashDays(java.util.Date v, int substep) {
        if (userKeyBytes==null) {
            userKeyBytes = userKey.getBytes(StandardCharsets.UTF_8);
        }
        final PureJavaCrc32 crc = new PureJavaCrc32();
        crc.update(dateFormat.format(v).getBytes(StandardCharsets.UTF_8));
        crc.update(userKeyBytes);
        if (substep > 0) {
            crc.update(Integer.toHexString(substep).getBytes(StandardCharsets.UTF_8));
        }
        long crcval = crc.getValue();
        if (crcval < 0) crcval = -1L * crcval;
        return 1 + (int) (crcval % 364L);
    }

    @Override
    public boolean isIterationsSupported() {
        return false;
    }

    @Override
    public Object exec(Object in, int iteration) {
        return exec(in);
    }

    private static enum FieldFormat {
        SqlDate,
        SqlTimestamp,
        String
    }

}
