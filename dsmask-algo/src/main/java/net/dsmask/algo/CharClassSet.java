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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.jdom2.Element;
import net.dsmask.util.JdomHelpers;

/**
 * The set of character classes for Format Preserving Encryption
 * Java algorithm implementation
 * @author mzinal
 */
public class CharClassSet {

    private final String name;
    private final List<Entry> entries;

    public static final Entry EMPTY_ENTRY = new Entry("", new Range[]{});

    public static final CharClassSet DEFAULT_ENGLISH = new CharClassSet (
            "default-english", Arrays.asList(
                    new Entry("small-latin", new Range('a', 'z')),
                    new Entry("large-latin", new Range('A', 'Z')),
                    new Entry("numbers", new Range('0', '9'))
            ));

    public static final CharClassSet DEFAULT_RUSSIAN = new CharClassSet (
            "default-russian", Arrays.asList(
                    new Entry("small-latin", new Range('a', 'z')),
                    new Entry("large-latin", new Range('A', 'Z')),
                    new Entry("small-cyrillic", new Range[] {
                            new Range('а', 'я'), new Range('ё') }),
                    new Entry("large-cyrillic", new Range[] {
                            new Range('А', 'Я'), new Range('Ё') }),
                    new Entry("numbers", new Range('0', '9'))
            ));

    public CharClassSet(String name, List<Entry> entries) {
        this.name = name;
        this.entries = Collections.unmodifiableList(entries);
    }

    public String getName() {
        return name;
    }

    /**
     * Determine a class by the codepoint.
     * Returns EMPTY_ENTRY if the codepoint does not fall into any other class.
     * @param codePoint Current code point
     * @return Entry for a particular character class, or EMPTY_ENTRY
     */
    public Entry findClass(int codePoint) {
        for (Entry e : entries) {
            for (Range r : e.ranges) {
                if (codePoint >= r.charBegin && codePoint <= r.charEnd)
                    return e;
            }
        }
        return EMPTY_ENTRY;
    }

    /**
     * Load the character class set from an XML document
     * @param path  Path to dictionary directory
     * @param name  Name of a character set class
     * @return Loaded character class set object
     * @throws Exception In case the XML document was not found or
     *      has invalid format
     */
    public static CharClassSet load(String path, String name)
            throws Exception {
        return load(new File(path, "ccs-" + name + ".xml"));
    }

    /**
     * Load the character class set from an XML document
     * @param fileName  Path to configuration file
     * @return Character class set object
     * @throws Exception In case the XML document was not found or has invalid format
     */
    public static CharClassSet load(String fileName)
            throws Exception {
        return load(new File(fileName));
    }

    /**
     * Load the character class set from an XML document
     * @param file  Path to configuration file
     * @return Character class set object
     * @throws Exception In case the XML document was not found or has invalid format
     */
    public static CharClassSet load(File file)
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
     * Load the character class set from an XML document
     * @param is Input stream containing an XML document
     * @return Character class set object
     * @throws Exception In case the XML has invalid format
     */
    public static CharClassSet load(InputStream is) throws Exception {
        return load(new org.jdom2.input.SAXBuilder().build(is).getRootElement());
    }

    /**
     * Load the character class set from a JDOM tree
     * @param config JDOM tree containing the character class definition
     * @return Character class set object
     * @throws Exception In case the JDOM tree is incorrect
     */
    public static CharClassSet load(org.jdom2.Element config) throws Exception {
        final String name = config.getAttributeValue("name");
        if (name==null) {
            throw new Exception("Character class set name was not defined");
        }
        final List<org.jdom2.Element> inputClasses =
                config.getChildren("char-class");
        if (inputClasses==null || inputClasses.isEmpty())
            throw new Exception("Empty character class set " + name);
        final List<Entry> entries = new ArrayList<>();
        for ( org.jdom2.Element ccls : inputClasses ) {
            Entry e = parseEntry(name, ccls);
            if (e!=null)
                entries.add(e);
        }
        return new CharClassSet(name, entries);
    }

    private static Entry parseEntry(String setName, Element ccls) throws Exception {
        String charClassName = ccls.getAttributeValue("name");
        if (charClassName==null || charClassName.length()==0)
            charClassName = "<undefined>";
        List<org.jdom2.Element> inputRanges = ccls.getChildren("char-range");
        if (inputRanges==null || inputRanges.isEmpty())
            return null;
        final int rangeCount = inputRanges.size();
        final Range[] ranges = new Range[rangeCount];
        for (int pos = 0; pos<rangeCount; ++pos) {
            org.jdom2.Element inputRange = inputRanges.get(pos);
            String charBegin = JdomHelpers.getCharVal("begin", inputRange);
            String charEnd = JdomHelpers.getCharVal("end", inputRange);
            if (charBegin==null || charEnd==null) {
                throw new Exception("Missing attributes for range in "
                        + "character class " + setName + "/" + charClassName);
            }
            ranges[pos] = new Range(charBegin.codePointAt(0),
                    charEnd.codePointAt(0));
        }
        return new Entry(charClassName, ranges);
    }

    /**
     * Character range
     */
    public static class Range {
        final int charBegin;
        final int charEnd;
        final int size;

        public Range(char single) {
            this((int)single);
        }

        public Range(char charBegin, char charEnd) {
            this((int)charBegin, (int)charEnd);
        }

        public Range(int single) {
            this(single, single);
        }

        public Range(int charBegin, int charEnd) {
            if (charBegin <= charEnd) {
                this.charBegin = charBegin;
                this.charEnd = charEnd;
            } else {
                this.charBegin = charEnd;
                this.charEnd = charBegin;
            }
            this.size = 1 + this.charEnd - this.charBegin;
        }
    }

    /**
     * Character class, composed of multiple character ranges
     */
    public static class Entry {
        final String name;
        final Range[] ranges;
        final int[] chars;

        public Entry(String name, Range range) {
            this(name, new Range[] { range });
        }

        public Entry(String name, Range[] ranges) {
            this.name = name;
            this.ranges = ranges;
            int totalSize = 0;
            for (Range r : ranges)
                totalSize += r.size;
            this.chars = new int[totalSize];
            int pos = 0;
            for (Range r : ranges) {
                for (int curChar = r.charBegin; curChar <= r.charEnd; ++curChar) {
                    this.chars[pos] = curChar;
                    ++pos;
                }
            }
        }

        public boolean isEmpty() {
            return (chars.length == 0);
        }

        public int getLength() {
            return chars.length;
        }

        public int getCodePoint(int pos) {
            return chars[pos];
        }
    }

}
