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
package com.ibm.dsmask.jconf.impl;

import java.util.List;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import com.ibm.dsmask.jconf.beans.*;

/**
 * Implements logic to read the metadata from IGC repository database.
 * @author zinal
 */
public class MetadataIgcReader implements AutoCloseable {

    private final Connection conn;
    private final SqlText sqlText;

    public MetadataIgcReader(String url, String userName, String password)
            throws Exception {
        this.conn = DriverManager.getConnection(url, userName, password);
        try {
            this.conn.setAutoCommit(false);
            this.conn.setReadOnly(true);
        } catch(Exception ex) {}
        this.sqlText = newSqlText();
    }

    /**
     * Reads all table definitions, including the fields and associated dataclasses.
     * Unmarked fields (e.g. those without dataclass/term labels) are ignored and are not loaded.
     * @return List of table definitions, including the field names and their labels.
     * @throws Exception
     */
    public List<TableInfo> readTables() throws Exception {
        final Map<String, TableInfo> work;
        try (PreparedStatement ps = conn.prepareStatement(sqlText.fieldDCS)) {
            try (ResultSet rs = ps.executeQuery()) {
                work = readTableDcs(rs);
            }
        }
        try (PreparedStatement ps = conn.prepareStatement(sqlText.fieldTerms)) {
            try (ResultSet rs = ps.executeQuery()) {
                appendTableTerms(rs, work);
            }
        }
        return new ArrayList<>(work.values());
    }

    private Map<String, TableInfo> readTableDcs(ResultSet rs) throws Exception {
        Map<String, TableInfo> work = new HashMap<>();
        while (rs.next()) {
            final String db = Utils.lower(rs.getString(1));
            final String schema = Utils.lower(rs.getString(2));
            final String table = Utils.lower(rs.getString(3));
            final String field = Utils.lower(rs.getString(4));
            final String clazz = Utils.lower(rs.getString(5));
            final String key = db + "." + schema + "." + table;
            TableInfo ti = work.get(key);
            if (ti==null) {
                ti = new TableInfo();
                ti.setDatabase(db);
                ti.setName(schema + "." + table);
                work.put(key, ti);
            }
            FieldInfo fi = ti.getField(field);
            if (fi==null) {
                fi = new FieldInfo(field);
                ti.addField(fi);
            }
            fi.addDc(clazz);
        }
        return work;
    }

    private void appendTableTerms(ResultSet rs, Map<String, TableInfo> work)
            throws Exception {
        while (rs.next()) {
            final String db = Utils.lower(rs.getString(1));
            final String schema = Utils.lower(rs.getString(2));
            final String table = Utils.lower(rs.getString(3));
            final String field = Utils.lower(rs.getString(4));
            final String clazz = Utils.lower(rs.getString(5));
            final String key = db + "." + schema + "." + table;
            TableInfo ti = work.get(key);
            if (ti==null)
                continue;
            FieldInfo fi = ti.getField(field);
            if (fi!=null)
                fi.addDc(clazz);
        }
    }

    /**
     * Generates the list of dataclass rules from IGC database content.
     * @return List of dataclass rule table entries.
     * @throws Exception
     */
    public List<DataClassRules.Entry> readDataClassRules() throws Exception {
        final List<DataClassRules.Entry> retval = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sqlText.allDCS)) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    readOneDcs(retval, rs);
                }
            }
        }
        try (PreparedStatement ps = conn.prepareStatement(sqlText.allTerms)) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    readOneDcs(retval, rs);
                }
            }
        }
        return retval;
    }

    /**
     * Generates the list of dataclass rules from IGC database content.
     * @param dcr The list of rules into which the new ones are added.
     * @throws Exception
     */
    public void readDataClassRules(DataClassRules dcr) throws Exception {
        for (DataClassRules.Entry e : readDataClassRules())
            dcr.addEntry(e);
    }

    private void readOneDcs(List<DataClassRules.Entry> rules, ResultSet rs) throws Exception {
        final String dcsname = Utils.lower(rs.getString(1));
        final String dcsmode = rs.getString(2);
        if (dcsname.length() > 0 && DataClassMode.isValid(dcsmode)) {
            final DataClassRules.Entry e = new DataClassRules.Entry(
                DataClassMode.getMode(dcsmode), dcsname, false
            );
            rules.add(e);
        }
    }

    @Override
    public void close() {
        Utils.close(conn);
    }

    private SqlText newSqlText() throws Exception {
        try (InputStream stream = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("igc-queries.xml")) {
            Element root = new SAXBuilder().build(stream).getRootElement();
            String allDCS = root.getChildTextTrim("select-all-dcs");
            String allTerms = root.getChildTextTrim("select-all-terms");
            String fieldDCS = root.getChildTextTrim("select-field-dcs");
            String fieldTerms = root.getChildTextTrim("select-field-terms");
            return new SqlText(allDCS, allTerms, fieldDCS, fieldTerms);
        }
    }

    private static class SqlText {
        final String allDCS;
        final String allTerms;
        final String fieldDCS;
        final String fieldTerms;

        public SqlText(String allDCS, String allTerms,
                String fieldDCS, String fieldTerms) {
            this.allDCS = allDCS;
            this.allTerms = allTerms;
            this.fieldDCS = fieldDCS;
            this.fieldTerms = fieldTerms;
        }
    }

}
