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
package ia.custom.ru;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Вспомогательный алгоритм для дополнения справочника фамилий мужскими
 * и женскими формами.
 * 
 * @author zinal
 */
public class NamesTool {

    private DcsDict input = null;
    private final List<String> pending = new ArrayList<>();

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("USAGE: java -jar ia-custom-ru-1.0.jar "
                    + "INFILE OUTFILE");
            System.exit(2);
        }
        try {
            new NamesTool() . run(args[0], args[1]);
        } catch(Exception ex) {
            ex.printStackTrace(System.err);
            System.exit(1);
        }
    }

    private void run(String inFile, String outFile) throws Exception {
        pending.clear();
        input = new DcsDict(new File(inFile));
        int sizeBefore = input.getEntries().size();
        for (String value : input.getEntries()) {
            handleSuffix(value, "ов", "ова");
            handleSuffix(value, "ев", "ева");
            handleSuffix(value, "ин", "ина");
            handleSuffix(value, "тый", "тая");
            handleSuffix(value, "сый", "сая");
            handleSuffix(value, "ный", "ная");
            handleSuffix(value, "кий", "кая");
            handleSuffix(value, "гий", "гая");
        }
        for (String value : pending) {
            input.add(value);
        }
        int sizeAfter = input.getEntries().size();
        if (sizeBefore < sizeAfter) {
            System.out.println("*** Added values: " + (sizeAfter - sizeBefore));
        }
        input.save(new File(outFile));
    }

    private void handleSuffix(String value, String male, String female) {
        String candidate = null;
        if (value.length() > male.length() && value.endsWith(male)) {
            candidate = value.substring(0, value.length() - male.length()) + female;
        } else if (value.length() > female.length() && value.endsWith(female)) {
            candidate = value.substring(0, value.length() - female.length()) + male;
        }
        if (candidate != null) {
            if (!input.containsDirect(candidate))
                pending.add(candidate);
        }
    }

}
