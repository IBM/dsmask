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
package net.dsmask.algo;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import net.dsmask.util.JdomHelpers;
import net.dsmask.util.DsMaskUtil;

/**
 * Translation table for character substitution.
 * @author zinal
 */
public class CharzTable {

    private final String name;
    private final List<Range> ranges;
    private final Map<Integer, Integer> direct;

    public CharzTable(String name, List<Range> ranges) {
        this.name = name;
        this.ranges = ranges;
        this.direct = buildDirect(ranges);
    }

    public String getName() {
        return name;
    }

    public List<Range> getRanges() {
        return ranges;
    }

    /**
     * Translate a single character according to the translation table.
     * Range scan implementation, not using the direct mapping.
     * @param input Input character
     * @return Translated character
     */
    public int translateByRange(int input) {
        for (Range r : ranges) {
            if (r.src <= input) {
                int diff = input - r.src;
                if (diff < r.size)
                    return r.dst + diff;
            }
        }
        return input;
    }

    /**
     * Translate a single character according to the translation table.
     * @param input Input character
     * @return Translated character
     */
    public int translate(int input) {
        return direct.getOrDefault(input, input);
    }

    private static Map<Integer, Integer> buildDirect(List<Range> ranges) {
        Map<Integer, Integer> d = new HashMap<>();
        for (Range r : ranges) {
            for (int ix = 0; ix < r.size; ++ix) {
                int srcCP = r.src + ix;
                if (! d.containsKey(srcCP))
                    d.put(srcCP, r.dst + ix);
            }
        }
        return d;
    }

    /**
     * Load the character translation table from an XML document
     * @param path  Path to dictionary directory
     * @param name  Name of a character translation table
     * @return Loaded character translation table object
     * @throws Exception In case the XML document was not found or
     *      has invalid format
     */
    public static CharzTable load(String path, String name)
            throws Exception {
        return load(new File(path, "charz-" + name + ".xml"));
    }

    /**
     * Load the character translation table from an XML document
     * @param fileName  Path to configuration file
     * @return Loaded character translation table object
     * @throws Exception In case the XML document was not found or has invalid format
     */
    public static CharzTable load(String fileName)
            throws Exception {
        return load(new File(fileName));
    }

    /**
     * Load the character translation table from an XML document
     * @param file  Path to configuration file
     * @return Loaded character translation table object
     * @throws Exception In case the XML document was not found or has invalid format
     */
    public static CharzTable load(File file)
            throws Exception {
        // Read an XML configuration file
        final org.jdom2.Element config;
        try (FileInputStream fis = new FileInputStream(file)) {
            config = new org.jdom2.input.SAXBuilder().build(fis)
                    .getRootElement();
        }
        return load(config);
    }

    /**
     * Load the character translation table from an XML document
     * @param is Input stream containing an XML document to be loaded
     * @return Loaded character translation table object
     * @throws Exception In case the XML has invalid format
     */
    public static CharzTable load(InputStream is) throws Exception {
        return load(new org.jdom2.input.SAXBuilder().build(is).getRootElement());
    }

    /**
     * Load the character translation table from a JDOM tree
     * @param config JDOM tree containing the character translation table definition
     * @return Loaded character translation table object
     * @throws Exception In case the JDOM tree is incorrect
     */
    public static CharzTable load(org.jdom2.Element config) throws Exception {
        final String name = config.getAttributeValue("name");
        if (name==null) {
            throw new Exception("Character translation table name was not defined");
        }
        final List<Range> ranges = new ArrayList<>();
        final List<org.jdom2.Element> inputRanges =
                config.getChildren("char-range");
        if (inputRanges!=null) {
            for ( org.jdom2.Element re : inputRanges ) {
                Range r = parseRange(name, re);
                if (r!=null)
                    ranges.add(r);
            }
        }
        final List<org.jdom2.Element> inputMaps =
                config.getChildren("char-map");
        if (inputMaps!=null) {
            for ( org.jdom2.Element m : inputMaps ) {
                parseMap(name, m, ranges);
            }
        }
        if (ranges.isEmpty())
            throw new Exception("Empty character translation table " + name);
        return new CharzTable(name, ranges);
    }

    private static String getSourceAttr(Element el) {
        String src = JdomHelpers.getCharVal("source", el);
        if (src==null)
            src = JdomHelpers.getCharVal("src", el);
        return src;
    }

    private static String getDestinationAttr(Element el) {
        String dst = JdomHelpers.getCharVal("destination", el);
        if (dst==null)
            dst = JdomHelpers.getCharVal("dest", el);
        if (dst==null)
            dst = JdomHelpers.getCharVal("dst", el);
        return dst;
    }

    private static int getLengthAttr(Element el, String tabName) throws Exception {
        String len = el.getAttributeValue("size");
        if (len==null)
            len = el.getAttributeValue("length");
        if (len==null)
            len = el.getAttributeValue("len");
        if (len==null)
            len = "1";
        final int size;
        try {
            size = Integer.parseInt(len.trim());
        } catch(NumberFormatException nfe) {
            throw new Exception("Illegal length value in "
                    + "character translation table " + tabName);
        }
        return size;
    }

    private static Range parseRange(String name, Element re) throws Exception {
        String src = getSourceAttr(re);
        String dst = getDestinationAttr(re);
        int len = getLengthAttr(re, name);
        if (src==null || dst==null) {
            throw new Exception("Missing attributes for range in "
                    + "character translation table " + name);
        }
        return new Range(src.codePointAt(0), dst.codePointAt(0), len);
    }

    private static void parseMap(String name, Element m, List<Range> ranges) throws Exception {
        String srcString = m.getAttributeValue("src");
        String dstString = m.getAttributeValue("dst");
        if (srcString==null || dstString==null) {
            throw new Exception("Missing attributes for map in "
                    + "character translation table " + name);
        }
        int[] src = srcString.codePoints().toArray();
        int[] dst = dstString.codePoints().toArray();
        if (src.length != dst.length){
            throw new Exception("Source and destination map sizes are different"
                    + " (" + String.valueOf(src.length) + "!="
                    + String.valueOf(dst.length) + ") "
                    + "in character translation table " + name);
        }
        final TreeMap<Integer, Integer> sorted = new TreeMap<>();
        for (int pos=0; pos<src.length; ++pos)
            sorted.put(src[pos], dst[pos]);
        Range r = null;
        for (Map.Entry<Integer,Integer> me : sorted.entrySet()) {
            if (r==null) {
                // create new range for a single character translation
                r = new Range(me.getKey(), me.getValue());
            } else if ( (r.src + r.size == me.getKey())
                        && (r.dst + r.size == me.getValue()) ) {
                // extend the current range by one character
                r = new Range(r.src, r.dst, r.size + 1);
            } else {
                // add the completed range
                ranges.add(r);
                // and create the new one
                r = new Range(me.getKey(), me.getValue());
            }
        }
        if (r!=null)
            ranges.add(r);
    }

    public org.jdom2.Element dump() {
        org.jdom2.Element jr = new org.jdom2.Element("char-trans-table");
        jr.setAttribute("name", name);
        for (Range r : ranges) {
            org.jdom2.Element je = new org.jdom2.Element("char-range");
            je.setAttribute("size", String.valueOf(r.size));
            je.setAttribute("src-char", DsMaskUtil.fromCodepoint(r.src));
            je.setAttribute("src-hex", Integer.toHexString(r.src));
            je.setAttribute("dst-char", DsMaskUtil.fromCodepoint(r.dst));
            je.setAttribute("dst-hex", Integer.toHexString(r.dst));
            jr.addContent(je);
        }
        return jr;
    }

    public void dump(java.io.PrintStream ps) throws Exception {
        new XMLOutputter(Format.getPrettyFormat())
                .output(new org.jdom2.Document( dump() ), ps);
    }

    public void dump(java.io.OutputStream os) throws Exception {
        new XMLOutputter(Format.getPrettyFormat().setEncoding("UTF-8"))
                .output(new org.jdom2.Document( dump() ), os);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("CharzTable(").append(name).append("): ");
        for (Range range : ranges) {
            sb.append(range.toString());
            sb.append(",");
        }
        sb.append("!!");
        return sb.toString();
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 19 * hash + Objects.hashCode(this.direct);
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
        final CharzTable other = (CharzTable) obj;
        if (!Objects.equals(this.direct, other.direct)) {
            return false;
        }
        return true;
    }

    /**
     * Character translation range
     */
    public static class Range {
        final int src;
        final int dst;
        final int size;

        public Range(char src, char dst) {
            this((int)src, (int)dst, 1);
        }

        public Range(char srcBegin, char dstBegin, int size) {
            this((int)srcBegin, (int)dstBegin, size);
        }

        public Range(int src, int dst) {
            this(src, dst, 1);
        }

        public Range(int src, int dst, int size) {
            this.src = src;
            this.dst = dst;
            this.size = (size>0) ? size : 1;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder();
            sb.append("[");
            for (int i=0; i<size; ++i)
                DsMaskUtil.appendCodepoint(sb, src + i);
            sb.append("] -> [");
            for (int i=0; i<size; ++i)
                DsMaskUtil.appendCodepoint(sb, dst + i);
            sb.append("]");
            return sb.toString();
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 53 * hash + this.src;
            hash = 53 * hash + this.size;
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
            final Range other = (Range) obj;
            if (this.src != other.src) {
                return false;
            }
            if (this.dst != other.dst) {
                return false;
            }
            if (this.size != other.size) {
                return false;
            }
            return true;
        }

    }
}
