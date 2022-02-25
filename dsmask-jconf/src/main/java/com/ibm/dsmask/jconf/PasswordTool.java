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
package com.ibm.dsmask.jconf;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Scanner;
import com.ibm.dsmask.util.PasswordVault;

/**
 *
 * @author zinal
 */
public class PasswordTool {

    private static final org.slf4j.Logger LOG =
            org.slf4j.LoggerFactory.getLogger(PasswordTool.class);

    public static void main(String[] args) {
        try {
            if (args.length == 0) {
                printUsage();
                System.exit(1);
            }
            final Mode mode = Mode.fromString(args[0]);
            if (mode==null) {
                printUsage();
                System.exit(1);
            }
            switch (mode) {
                case LIST:
                    if (args.length != 1) {
                        printUsage();
                        System.exit(1);
                    } else {
                        doList();
                    }
                    break;
                case SET:
                    if (args.length != 3) {
                        printUsage();
                        System.exit(1);
                    } else {
                        doSet(args[1], args[2]);
                    }
                    break;
                case IMPORT:
                    if (args.length != 2) {
                        printUsage();
                        System.exit(1);
                    } else {
                        doImport(args[1]);
                    }
                    break;
                case DELETE:
                    if (args.length != 2) {
                        printUsage();
                        System.exit(1);
                    } else {
                        doDelete(args[1]);
                    }
                    break;
            }
        } catch(Exception ex) {
            LOG.error("Operation failed", ex);
            System.exit(3);
        }
    }

    private static void doList() {
        int counter = 0;
        final PasswordVault vault = new PasswordVault();
        for (String key : vault.enumKeys()) {
            PasswordVault.Entry e = vault.getEntry(key);
            LOG.info("list: {} -> {} : {}", key, e.login, "***");
            ++counter;
        }
        LOG.info("Total entries listed: {}", counter);
    }

    private static void doSet(String key, String login) {
        if (System.console() == null)
            throw new RuntimeException("Cannot enter password - console not available");
        final PasswordVault vault = new PasswordVault();
        char[] pass1 = System.console().readPassword("Please enter password:  ");
        char[] pass2 = System.console().readPassword("Please repeat password: ");
        if ( ! Arrays.equals(pass1, pass2) )
            throw new RuntimeException("Passwords do not match!");
        vault.putEntry(key, login, new String(pass1));
        vault.save();
        LOG.info("Entry configured: {}", key);
    }

    private static void doImport(String fileName) throws Exception {
        if ("-".equals(fileName) || fileName==null || fileName.length()==0) {
            doImport(System.in);
        } else {
            try (FileInputStream fis = new FileInputStream(fileName)) {
                doImport(fis);
            }
        }
    }

    private static void doImport(InputStream is) throws Exception {
        int counter = 0;
        final PasswordVault vault = new PasswordVault();
        Scanner scanner = new Scanner(new InputStreamReader(is, StandardCharsets.UTF_8));
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            String[] items = line.split("\t");
            if (items.length != 3)
                continue;
            if (items[0].trim().startsWith("#"))
                continue;
            vault.putEntry(items[0], items[1], items[2]);
            LOG.info("Entry configured: {}", items[0]);
            ++counter;
        }
        if (counter > 0)
            vault.save();
        LOG.info("Total entries imported: {}", counter);
    }

    private static void doDelete(String key) {
        final PasswordVault vault = new PasswordVault();
        PasswordVault.Entry e = vault.getEntry(key);
        if (e == null) {
            LOG.error("Entry not found: {}", key);
            System.exit(2);
        }
        vault.removeEntry(key);
        vault.save();
        LOG.info("Entry removed: {}", key);
    }

    private static void printUsage() {
        System.out.println("USAGE:");
        System.out.println("\t\t" + PasswordTool.class.getName()
                + " LIST");
        System.out.println("\t\t" + PasswordTool.class.getName()
                + " SET key login");
        System.out.println("\t\t" + PasswordTool.class.getName()
                + " IMPORT { passwordFile | - }");
        System.out.println("\t\t" + PasswordTool.class.getName()
                + " DELETE key");
    }

    static enum Mode {
        LIST,
        SET,
        IMPORT,
        DELETE;

        static Mode fromString(String v) {
            for (Mode m : Mode.values()) {
                if (m.name().equalsIgnoreCase(v))
                    return m;
            }
            return null;
        }
    }

}
