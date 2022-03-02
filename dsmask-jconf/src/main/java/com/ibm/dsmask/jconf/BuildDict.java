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
package com.ibm.dsmask.jconf;

import java.io.FileInputStream;
import java.util.Properties;
import com.ibm.dsmask.jconf.beans.Utils;
import com.ibm.dsmask.jconf.fio.FioGenerator;
import net.dsmask.DsMaskVersion;

/**
 * Dictionary builder entry point.
 * @author zinal
 */
public class BuildDict {

    private static final org.slf4j.Logger LOG = Utils.logger(BuildDict.class);

    public static final String PROP_NAMES_M = "NamesMale";
    public static final String PROP_NAMES_F = "NamesFemale";
    public static final String PROP_FAM_M = "FamMale";
    public static final String PROP_FAM_F = "FamFemale";
    public static final String PROP_SALT = "SaltKey";
    public static final String PROP_DBNAME = "DbName";
    public static final String PROP_DBURL = "DbUrl";
    public static final String PROP_DBUSER = "DbUser";
    public static final String PROP_DBPASS = "DbPassword";
    public static final String PROP_TOTNAMES = "TotalNames";

    private final Properties props;

    public BuildDict(Properties props) {
        this.props = props;
    }

    public static void main(String[] args) {
        try {
            if (args.length != 1) {
                System.out.println("USAGE: " + BuildDict.class.getName()
                        + " jobfile.xml");
                System.exit(1);
            }

            LOG.info("DsMask {} BuildDict", DsMaskVersion.VERSION);

            final Properties props = new Properties();
            try (FileInputStream fis = new FileInputStream(args[0])) {
                props.loadFromXML(fis);
            }

            LOG.info("Starting FIO dictionary generator...");
            new BuildDict(props).run();
            LOG.info("FIO dictionary generator completed!");

        } catch(Exception ex) {
            LOG.error("FATAL: operation failed", ex);
            System.exit(1);
        }
    }

    private void run() throws Exception {
        try (FioGenerator fgen = new FioGenerator()) {
            fgen.loadNames(
                    props.getProperty(PROP_NAMES_M),
                    props.getProperty(PROP_NAMES_F),
                    props.getProperty(PROP_FAM_M),
                    props.getProperty(PROP_FAM_F)
            );
            LOG.info("Source data load completed.");
            fgen.setSortSalt(props.getProperty(PROP_SALT));
            final String dbName = props.getProperty(PROP_DBNAME);
            if (dbName!=null) {
                fgen.create(dbName);
            } else {
                final String dbUrl = props.getProperty(PROP_DBURL);
                final String dbUser = props.getProperty(PROP_DBUSER);
                final String dbPass = props.getProperty(PROP_DBPASS);
                if (dbUrl==null || dbUser==null || dbPass==null) {
                    throw new IllegalArgumentException("Incorrect configuration:"
                            + " either DbName or DbUrl must be specified");
                }
                fgen.openUrl(dbUrl, dbUser, dbPass);
            }
            LOG.info("Target database open.");
            fgen.generate(Integer.valueOf(props.getProperty(PROP_TOTNAMES)));
        }
    }

}
