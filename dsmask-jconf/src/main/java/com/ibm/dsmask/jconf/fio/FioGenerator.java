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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import com.ibm.dsmask.jconf.beans.Utils;
import com.ibm.dsmask.jconf.impl.DbUtils;

/**
 *
 * @author zinal
 */
public class FioGenerator extends DbUtils implements AutoCloseable {

    private static final org.slf4j.Logger LOG = Utils.logger(FioGenerator.class);

    private static final String[] SQL_CREATE = {
        "CREATE TABLE dict_fio("
            + "id INTEGER NOT NULL PRIMARY KEY, "
            + "nfull VARCHAR(150) NOT NULL, "
            + "nfirst VARCHAR(40) NOT NULL, "
            + "nmiddle VARCHAR(40) NOT NULL, "
            + "nlast VARCHAR(40) NOT NULL,"
            + "sex CHAR(1) NOT NULL)",
        "CREATE TABLE dict_fio_male("
            + "id INTEGER NOT NULL PRIMARY KEY, "
            + "nfull VARCHAR(150) NOT NULL, "
            + "nfirst VARCHAR(40) NOT NULL, "
            + "nmiddle VARCHAR(40) NOT NULL, "
            + "nlast VARCHAR(40) NOT NULL)",
        "CREATE TABLE dict_fio_female("
            + "id INTEGER NOT NULL PRIMARY KEY, "
            + "nfull VARCHAR(150) NOT NULL, "
            + "nfirst VARCHAR(40) NOT NULL, "
            + "nmiddle VARCHAR(40) NOT NULL, "
            + "nlast VARCHAR(40) NOT NULL)",
        "CREATE TABLE dict_name_last("
            + "id INTEGER NOT NULL PRIMARY KEY, "
            + "val VARCHAR(40) NOT NULL, "
            + "sex CHAR(1) NOT NULL)",
        "CREATE TABLE dict_name_first("
            + "id INTEGER NOT NULL PRIMARY KEY, "
            + "val VARCHAR(40) NOT NULL, "
            + "sex CHAR(1) NOT NULL)",
        "CREATE INDEX dict_name_first_ix1 "
            + "ON dict_name_first(val)",
        "CREATE TABLE dict_name_middle("
            + "id INTEGER NOT NULL PRIMARY KEY, "
            + "val VARCHAR(40) NOT NULL, "
            + "sex CHAR(1) NOT NULL)"
    };

    private Connection connection;
    private PreparedStatement psAddMale;
    private PreparedStatement psAddFemale;
    private PreparedStatement psAddAny;

    private String sortSalt;

    private final NamesData dataMale = new NamesData();
    private final NamesData dataFemale = new NamesData();

    private int countAll = 0;
    private int countMale = 0;
    private int countFemale = 0;

    private NamesSource controller = null;
    private long lastUpdated = 0L;

    @Override
    public void close() {
        if (controller!=null) {
            controller.stop();
            controller = null;
        }
        Utils.close(psAddMale);  psAddMale = null;
        Utils.close(psAddFemale);  psAddFemale = null;
        Utils.close(psAddAny);  psAddAny = null;
        if (connection != null) {
            try {
                connection.rollback();
            } catch(Exception ex) {
            }
            try {
                connection.close();
            } catch(Exception ex) {
                LOG.warn("Error closing H2 connection", ex);
            }
            connection = null;
        }
    }

    /**
     * Create a new database, throwing the exception if one exists.
     * @param pathname
     * @throws Exception
     */
    public void create(String pathname) throws Exception {
        deleteFiles(pathname);
        final Connection con = DriverManager.getConnection
            (makeConnectionUrl(pathname)
                + ";COMPRESS=YES;PAGE_SIZE=32768;CACHE_SIZE=65536;LOCK_MODE=0");
        try {
            con.setAutoCommit(false);
            createTables(con, SQL_CREATE);
            con.commit();
        } catch(Exception ex) {
            try { con.rollback(); } catch(Exception xx) {}
            try { con.close(); } catch(Exception xx) {}
            deleteFiles(pathname);
            throw new Exception("Table creation failed", ex);
        }
        this.connection = con;
    }

    public void openUrl(String url, String username, String password)
            throws Exception {
        final Connection con
                = DriverManager.getConnection(url, username, password);
        try {
            con.setAutoCommit(false);
            createTables(con, SQL_CREATE);
            con.commit();
        } catch(Exception ex) {
            try { con.rollback(); } catch(Exception xx) {}
            try { con.close(); } catch(Exception xx) {}
            throw new Exception("Table creation failed", ex);
        }
        this.connection = con;
    }

    public void setSortSalt(String sortSalt) {
        this.sortSalt = sortSalt;
    }

    public void loadNames(String fNamesMale, String fNamesFemale,
            String fLastMale, String fLastFemale) throws Exception {
        if (fNamesMale==null || fNamesFemale==null ||
                fLastMale==null || fLastFemale==null) {
            throw new IllegalArgumentException("Missing input filenames, "
                    + "check job configuration");
        }
        List<NamesMaleBean> firstMale = NamesMaleBean.readExtended(fNamesMale);
        List<NamesBean> firstFemale = NamesMaleBean.readSimple(fNamesFemale);
        List<NamesBean> lastMale = NamesBean.readSimple(fLastMale);
        List<NamesBean> lastFemale = NamesBean.readSimple(fLastFemale);

        dataMale.last = lastMale;
        dataMale.first = new ArrayList<>();
        dataMale.middle = new ArrayList<>();
        dataFemale.last = lastFemale;
        dataFemale.first = firstFemale;
        dataFemale.middle = new ArrayList<>();

        for (NamesMaleBean nm : firstMale) {
            dataMale.first.add(new NamesBean(nm.getName()));
            dataMale.middle.add(new NamesBean(nm.getMidMale()));
            dataFemale.middle.add(new NamesBean(nm.getMidFemale()));
        }

        LOG.info("Input dictionaries: {}/{}/{} male, {}/{}/{} female.",
                dataMale.first.size(), dataMale.last.size(), dataMale.middle.size(),
                dataFemale.first.size(), dataFemale.last.size(), dataFemale.middle.size());
    }

    public void generate(int count) throws Exception {
        saveNames();
        LOG.info("Name dictionaries saved to database.");
        if (controller==null) {
            controller = new NamesSourceV2(sortSalt, dataMale, dataFemale);
            controller.start();
            LOG.info("Started controller {}", controller.toString());
        }
        lastUpdated = System.currentTimeMillis();
        int steps = 0;
        for (int i=0; i<count; ++i) {
            writeRow(true, controller.nextMale());
            writeRow(false, controller.nextFemale());
            if (checkCommit(++steps))
                steps = 0;
        } // for (...)
        if (steps>0) {// Have some rows still uncommitted
            checkCommit(-1);
        }
    }

    /**
     * Commit if number of inserted rows exceed threshold.
     * @param steps Number of inserted rows, or -1 on final call
     * @return true, if committed, false otherwise
     * @throws Exception
     */
    private boolean checkCommit(int steps)
            throws Exception {
        if (steps>=0 && steps<500)
            return false;
        psAddMale.executeBatch();
        psAddFemale.executeBatch();
        psAddAny.executeBatch();
        connection.commit();
        long tv = System.currentTimeMillis();
        if (steps<0 || tv - lastUpdated >= 5000L) {
            int dupCount = controller.getDuplicateCount();
            if (dupCount > 0) {
                LOG.info("Generated {} names, {} duplicates",
                        countAll, dupCount);
            } else {
                LOG.info("Generated {} names", countAll);
            }
            lastUpdated = tv;
        }
        return true;
    }

    private void writeRow(boolean male, NameValues nv) throws Exception {
        if (psAddAny==null) {
            psAddAny = connection.prepareStatement("INSERT "
                        + "INTO dict_fio "
                        + "(id, nfull, nfirst, nmiddle, nlast, sex) "
                        + "VALUES (?, ?, ?, ?, ?, ?)");
        }
        psAddAny.setInt(1, countAll++);
        psAddAny.setString(2, nv.full);
        psAddAny.setString(3, nv.first);
        psAddAny.setString(4, nv.middle);
        psAddAny.setString(5, nv.last);
        psAddAny.setString(6, male ? "M" : "T");
        psAddAny.addBatch();
        final PreparedStatement ps;
        if (male) {
            if (psAddMale==null) {
                psAddMale = connection.prepareStatement("INSERT "
                        + "INTO dict_fio_male "
                        + "(id, nfull, nfirst, nmiddle, nlast) "
                        + "VALUES (?, ?, ?, ?, ?)");
            }
            ps = psAddMale;
        } else {
            if (psAddFemale==null) {
                psAddFemale = connection.prepareStatement("INSERT "
                        + "INTO dict_fio_female "
                        + "(id, nfull, nfirst, nmiddle, nlast) "
                        + "VALUES (?, ?, ?, ?, ?)");
            }
            ps = psAddFemale;
        }
        if (male) {
            ps.setInt(1, countMale++);
        } else {
            ps.setInt(1, countFemale++);
        }
        ps.setString(2, nv.full);
        ps.setString(3, nv.first);
        ps.setString(4, nv.middle);
        ps.setString(5, nv.last);
        ps.addBatch();
    }

    private void savePair(String tabName, List<NamesBean> male,
            List<NamesBean> female) throws Exception {
        PreparedStatement ps = null;
        int index;
        try {
            index = 0;
            ps = connection.prepareStatement("INSERT INTO " + tabName
                    + "(id,val,sex) VALUES(?,?,?)");
            for (NamesBean nb : male) {
                ps.setInt(1, index);
                ps.setString(2, nb.getName());
                ps.setString(3, "M");
                ps.execute();
                ++index;
            }
            for (NamesBean nb : female) {
                ps.setInt(1, index);
                ps.setString(2, nb.getName());
                ps.setString(3, "F");
                ps.execute();
                ++index;
            }
            ps.close();
        } finally {
            Utils.close(ps);
        }
    }

    private void saveNames() throws Exception {
        savePair("dict_name_first", dataMale.first, dataFemale.first);
        savePair("dict_name_last", dataMale.last, dataFemale.last);
        savePair("dict_name_middle", dataMale.middle, dataFemale.middle);
    }

}
