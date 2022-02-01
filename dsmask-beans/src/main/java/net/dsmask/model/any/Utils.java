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
package net.dsmask.model.any;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Internal model-related utilities.
 * Mostly related to handling case-insensitive names.
 * @author zinal
 */
public class Utils {

    /**
     * Empty string - treated as a missing value.
     */
    public static final String NONE = "";

    public static String safe(String value) {
        return (value==null) ? NONE : value.trim();
    }

    public static String lower(String value) {
        return safe(value).toLowerCase();
    }

    public static boolean equalsCI(String a, String b) {
        return (a == b) || safe(a).equalsIgnoreCase(safe(b));
    }

    public static boolean equalsNL(String a, String b) {
        return Objects.equals(a, b)
                || (a==null && (b==null || b.length()==0))
                || (b==null && (a==null || a.length()==0));
    }

    public static boolean safeEquals(String a, String b) {
        return (a==b) || safe(a).equals(safe(b));
    }

    public static int hashCode(String o) {
        return o != null ? lower(o).hashCode() : 0;
    }
    
    /**
     * Compute the list of confidential fields in a table
     * @param table Input table
     * @return List of confidential fields (may be empty)
     */
    public static List<AnyField> getCondidentialFields(AnyTable table) {
        final List<AnyField> retval = new ArrayList<>();
        for (AnyField fi : table.getFields()) {
            for (AnyLabel label : fi.getLabels()) {
                if (label!=null && label.isConfidential()) {
                    retval.add(fi);
                    break; // no need to check other labels
                }
            }
        }
        return retval;
    }

    /**
     * Collect all labels from all the fields.
     * @param table Input table
     * @return Set of all labels assigned to the fields of the table.
     */
    public static Set<AnyLabel> getAllLabels(AnyTable table) {
        final Set<AnyLabel> dataClasses = new HashSet<>();
        table.getFields().forEach(fi -> {
            dataClasses.addAll(fi.getLabels());
        });
        return dataClasses;
    }

}
