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
package com.ibm.dsmask.apply;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;
import com.ibm.is.cc.javastage.api.*;

/**
 *
 * @author zinal
 */
public class DstMergeAlgo {

    /**
     * Collect group of fields from input record into a map of values
     * @param ir  Input record
     * @param mg  Group definition (field names + indexes)
     * @return Field names mapped to their values
     */
    public static Map<String, Object> collect(InputRecord ir, DstMergeGroup mg) {
        final Map<String, Object> retval = new HashMap<>();
        for (DstMergeGroup.Column c : mg.getSourceColumns()) {
            Object value = ir.getValue(c.getIndex());
            if (value!=null)
                retval.put(c.getName().toLowerCase(), value);
        }
        return retval;
    }

    /**
     * Pack a map of field values into a byte array
     * @param data Map, typically produced by the collect() method
     * @return Input map in the packed format
     */
    public static byte[] pack(Map<String, Object> data) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
                oos.writeObject(data);
            }
        } catch(Exception ex) {
            throw new RuntimeException("DstMergeAlgo.pack() failed", ex);
        }
        return baos.toByteArray();
    }

    /**
     * Unpack a map of field values from the byte array
     * @param input Packed format produced by pack() method
     * @return Field names mapped to their values
     */
    public static Map<String, Object> unpack(byte[] input) {
        if (input==null)
            return null;
        try {
            final ObjectInputStream ois = new ObjectInputStream(
                    new ByteArrayInputStream(input));
            Object value = ois.readObject();
            if (value==null)
                return null;
            if (!(value instanceof Map)) {
                throw new Exception("Incorrect packed object type: "
                        + value.getClass().getName());
            }
            boolean needConvert = false;
            for (Object key : ((Map<?,?>)value).keySet()) {
                if (key==null) {
                    throw new Exception("Null key in a map");
                }
                if (!(key instanceof String)) {
                    throw new Exception("Incorrect packed key type: "
                            + key.getClass().getName());
                }
                if (!needConvert) {
                    // Check whether the column name is defined in lower case
                    if (!key.toString().toLowerCase().equals(key))
                        needConvert = true;
                }
            }
            if (needConvert) {
                // Ensure the lower case of column names
                Map<String, Object> retval = new HashMap<>();
                for (Map.Entry<String, Object> me :
                        ((Map<String, Object>) value).entrySet()) {
                    retval.put(me.getKey().toLowerCase(), me.getValue());
                }
                return retval;
            }
            return (Map<String, Object>) value;
        } catch(Exception ex) {
            throw new RuntimeException("DstMergeAlgo.unpack() failed", ex);
        }
    }

}
