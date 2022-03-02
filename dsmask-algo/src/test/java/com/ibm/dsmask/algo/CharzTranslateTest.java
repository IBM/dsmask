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
package com.ibm.dsmask.algo;

import net.dsmask.algo.CharzTable;
import net.dsmask.algo.CharzTranslate;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author zinal
 */
public class CharzTranslateTest {

    public static final String CONFIG_ONE =
            "<?xml version='1.0' encoding='UTF-8' standalone='yes' ?>\n"
            + "<char-trans-table name='test'>"
            + "<char-range source-char='a' destination-char='A' size='26'/>"
            + "<char-range source-char='а' destination-char='А' size='32'/>"
            + "<!-- replace @ with ¥ -->"
            + "<char-range source-hex='40' destination-hex='A5' size='1'/>"
            + "</char-trans-table>";

    public static final String INPUT_ONE  = "abcdefghijklmnopqrstuvwxyz+/%$\n"
            + "абвгдеёжзийклмнопрстуфхцчшщъыьэюя\n"
            + "±!<>#$%^&*()[]{}_@";
    public static final String EXPECT_ONE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ+/%$\n"
            + "АБВГДЕёЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯ\n"
            + "±!<>#$%^&*()[]{}_¥";

    private CharzTable configOne = null;

    private CharzTable getConfigOne() {
        if (configOne == null) {
            try {
                configOne = CharzTable.load(
                        new ByteArrayInputStream(CONFIG_ONE.getBytes(StandardCharsets.UTF_8)));
            } catch(Exception ex) {
                throw new RuntimeException(ex);
            }
        }
        return configOne;
    }

    @Test
    public void checkLoadConfig() {
        configOne = null;
        CharzTable t = getConfigOne();
        Assert.assertEquals(3, t.getRanges().size());
    }

    @Test
    public void checkTranslateOne() {
        CharzTranslate algo = new CharzTranslate(getConfigOne());
        String input = INPUT_ONE;
        String output = algo.translate(input);
        Assert.assertEquals(EXPECT_ONE, output);
    }

    public static final String CONFIG_TWO =
            "<?xml version='1.0' encoding='UTF-8' standalone='yes' ?>\n"
            + "<char-trans-table name='test'>"
            + "<char-map src='abcdefghijklmnopqrstuvwxyz' "
            + "          dst='абсдефгхичклмнопцрстувшжыз'/>"
            + "</char-trans-table>";

    public static final String INPUT_TWO   = "ahtung!";
    public static final String EXPECT_TWO  = "ахтунг!";

    private CharzTable configTwo = null;

    private CharzTable getConfigTwo() {
        if (configTwo == null) {
            try {
                configTwo = CharzTable.load(
                        new ByteArrayInputStream(CONFIG_TWO.getBytes(StandardCharsets.UTF_8)));
//                System.out.println(configTwo.toString());
            } catch(Exception ex) {
                throw new RuntimeException(ex);
            }
        }
        return configTwo;
    }

    @Test
    public void checkTranslateTwo() {
        CharzTranslate algo = new CharzTranslate(getConfigTwo());
        String input = INPUT_TWO;
        String output = algo.translate(input);
        Assert.assertEquals(EXPECT_TWO, output);
    }

}
