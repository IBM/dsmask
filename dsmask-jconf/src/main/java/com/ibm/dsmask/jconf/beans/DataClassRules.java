/*
 * Copyright (c) IBM Corp. 2018, 2022.
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
package com.ibm.dsmask.jconf.beans;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * The list of rules over names of data classes to assign the mode.
 * @author zinal
 */
public class DataClassRules {

    // all entries are in the entries list
    private final List<Entry> entries = new ArrayList<>();
    // non-RX entries come to lookup table on prepare()
    private final Map<String, Entry> lookup = new HashMap<>();

    public DataClassRules() {
    }

    public DataClassRules(Collection<Entry> entries) {
        for (Entry e : entries)
            this.entries.add(e);
    }

    public List<Entry> getEntries() {
        return entries;
    }

    public void setEntries(List<Entry> entries) {
        this.entries.clear();
        for (Entry e : entries)
            this.entries.add(e);
    }

    public void addEntry(Entry entry) {
        entries.add(entry);
    }

    public void addEntry(DataClassMode mode, String value, boolean regexp) {
        entries.add(new Entry(mode, value, regexp));
    }

    /**
     * Prepare the rule set.
     * Compiles the regular expressions and populates the direct lookup table.
     * @throws Exception
     */
    public void prepare() throws Exception {
        for (Entry e : entries) {
            if (e.isRegexp()) {
                if (e.getPattern()==null) {
                    e.setPattern(Pattern.compile(e.getValue()));
                }
            } else {
                lookup.put(e.getValue(), e);
            }
        }
    }

    /**
     * Associate the data class to a particular mode with our rules.
     * @param dcname Data class name
     * @return Data class mode
     */
    public DataClassMode decide(String dcname) {
        dcname = Utils.lower(dcname);
        if (dcname.length()==0)
            return DataClassMode.Normal;
        // Direct name lookup. It always has priority over RX-based searches.
        Entry el = lookup.get(dcname);
        if (el!=null)
            return el.getMode();
        // RX lookup. Still has older direct search branch.
        for (Entry e : entries) {
            if (e.isRegexp()) {
                if (e.getPattern()==null)
                    throw new IllegalStateException("DataClassRules not prepared");
                if ( e.getPattern().matcher(dcname).matches() )
                    return e.getMode();
            } else {
                if (dcname.equals(e.getValue()))
                    return e.getMode();
            }
        }
        return DataClassMode.Normal;
    }

    /**
     * Gather data class information from the field descriptions,
     * and build the collection of data class descriptions.
     * @param tables Table (and field) descriptions
     * @return Data class descriptions
     */
    public Collection<DataClass> collect(Collection<TableInfo> tables) {
        final Map<String, DataClass> dcs = new HashMap<>();
        for (TableInfo ti : tables) {
            for (FieldInfo fi : ti.getFields()) {
                for (String dcname : fi.getDcs()) {
                    if (dcname.length()==0)
                        continue;
                    DataClass dcobj = dcs.get(dcname);
                    if (dcobj==null) {
                        final DataClassMode mode = decide(dcname);
                        dcobj = new DataClass(dcname, mode);
                        dcs.put(dcname, dcobj);
                    }
                }
            }
        }
        return dcs.values();
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 79 * hash + Objects.hashCode(this.entries);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final DataClassRules other = (DataClassRules) obj;
        if (!Objects.equals(this.entries, other.entries)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "DataClassRules{" + entries + '}';
    }

    public static class Entry {
        private DataClassMode mode;
        private String value;
        private boolean regexp;
        private transient Pattern pattern;

        public Entry() {
        }

        public Entry(DataClassMode mode, String value, boolean regexp) {
            this.mode = mode;
            this.value = Utils.lower(value);
            this.regexp = regexp;
        }

        public DataClassMode getMode() {
            return mode;
        }

        public void setMode(DataClassMode mode) {
            this.mode = mode;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = Utils.lower(value);
        }

        public boolean isRegexp() {
            return regexp;
        }

        public void setRegexp(boolean regexp) {
            this.regexp = regexp;
        }

        public Pattern getPattern() {
            return pattern;
        }

        public void setPattern(Pattern pattern) {
            this.pattern = pattern;
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 73 * hash + Objects.hashCode(this.mode);
            hash = 73 * hash + Objects.hashCode(this.value);
            hash = 73 * hash + (this.regexp ? 1 : 0);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Entry other = (Entry) obj;
            if (this.regexp != other.regexp) {
                return false;
            }
            if (!Objects.equals(this.value, other.value)) {
                return false;
            }
            if (this.mode != other.mode) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            return "{" +  mode + ", " + regexp + ", " + value + '}';
        }

    } // class Entry

}
