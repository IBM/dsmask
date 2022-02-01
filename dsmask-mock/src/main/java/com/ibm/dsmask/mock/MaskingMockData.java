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
package com.ibm.dsmask.mock;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import org.apache.commons.text.RandomStringGenerator;

/**
 *
 * @author zinal
 */
public class MaskingMockData {

    public static final String TAB1 = "schema1.table1";
    public static final String TAB2 = "schema2.table2";
    public static final String TAB3 = "schema3.table3";
    public static final String TAB_ANY = "schema.table";

    private final String tableName;
    private final List<MaskingMockColumn> columns;
    private List<Object[]> data;

    private final RandomStringGenerator randomCodes =
            new RandomStringGenerator.Builder().withinRange(
                    new char[][] { {'a', 'z'}, {'A', 'Z'}, {'0', '9'}})
                    .build();

    public MaskingMockData(String tableName) {
        this.tableName = tableName;
        this.columns = buildColumns(tableName);
        this.data = null;
    }

    public String getTableName() {
        return tableName;
    }

    public List<MaskingMockColumn> getColumns() {
        return columns;
    }

    public List<Object[]> getData() {
        if (data==null)
            data = buildData(tableName);
        return data;
    }

    private List<MaskingMockColumn> buildColumns(String tableName) {
        if (TAB1.equalsIgnoreCase(tableName)) {
            return builtTable1();
        } else if (TAB2.equalsIgnoreCase(tableName)) {
            return builtTable2();
        } else if (TAB3.equalsIgnoreCase(tableName)) {
            return builtTable3();
        } else {
            return buildTableAny();
        }
    }

    private List<Object[]> buildData(String tableName) {
        if (TAB1.equalsIgnoreCase(tableName)) {
            return dataTable1();
        } else if (TAB2.equalsIgnoreCase(tableName)) {
            return dataTable2();
        } else if (TAB3.equalsIgnoreCase(tableName)) {
            return dataTable3();
        } else {
            return dataDefault();
        }
    }

    private static MockDCS[] dcs(MockDCS... args) {
        return args;
    }

    /**
     * Generate table 3, to allow collision checks.
     * @return Columns for table 3.
     */
    private List<MaskingMockColumn> builtTable3() {
        List<MaskingMockColumn> cols = new ArrayList<>();
        cols.add(new MaskingMockColumn(1, "id", null));
        cols.add(new MaskingMockColumn(2, "code", dcs(MockDCS.SUPER_CODE)));
        return cols;
    }

    private List<Object[]> dataTable3() {
        final List<Object[]> retval = new ArrayList<>();
        for (int i=0; i<100000; ++i) {
            retval.add(new Object[] { i+1, randomCodes.generate(3) });
        }
        return retval;
    }

    private List<MaskingMockColumn> builtTable2() {
        List<MaskingMockColumn> cols = new ArrayList<>();
        cols.add(new MaskingMockColumn(1, "id", null));
        cols.add(new MaskingMockColumn(2, "nm_full", dcs(MockDCS.NAME_FULL)));
        cols.add(new MaskingMockColumn(3, "sex_code", dcs(MockDCS.SEX)));
        cols.add(new MaskingMockColumn(4, "doc_pass_num", dcs(MockDCS.PASS_NUM)));
        cols.add(new MaskingMockColumn(5, "doc_pass_date", dcs(MockDCS.PASS_DATE)));
        cols.add(new MaskingMockColumn(6, "doc_snils", dcs(MockDCS.SNILS)));
        cols.add(new MaskingMockColumn(7, "doc_inn", dcs(MockDCS.INN)));
        cols.add(new MaskingMockColumn(8, "date_birth", dcs(MockDCS.DOB, MockDCS.DATE)));
        cols.add(new MaskingMockColumn(9, "date_added", dcs(MockDCS.DATE)));
        cols.add(new MaskingMockColumn(10, "date_updated", dcs(MockDCS.DATE)));
        cols.add(new MaskingMockColumn(11, "card_num", dcs(MockDCS.CARD_PAN)));
        cols.add(new MaskingMockColumn(12, "card_code", dcs(MockDCS.CARD_CODE, MockDCS.CODE)));
        return cols;
    }

    private java.sql.Date makePassportDate(Calendar dob, int posNum) {
        final Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(dob.getTimeInMillis());
        cal.add(Calendar.YEAR, 17);
        cal.add(Calendar.DAY_OF_YEAR, 11 * posNum);
        return new java.sql.Date(cal.getTimeInMillis());
    }

    private List<Object[]> dataTable2() {
        int maxPos = FIO_MALE.length;
        if (maxPos > FIO_FEMALE.length)
            maxPos = FIO_FEMALE.length;
        int posNums = 0;
        int position = 0;
        Calendar cal = Calendar.getInstance();
        cal.set(1975, 0, 1);
        final List<Object[]> retval = new ArrayList<>();
        for (int i=0; i<maxPos; ++i) {
            cal.add(Calendar.YEAR, 1);
            cal.add(Calendar.DAY_OF_YEAR, 5);
            retval.add(new Object[] {
                ++position,
                FIO_MALE[i].toLowerCase(),
                "M",
                PASS_NUMS[posNums],
                makePassportDate(cal, posNums),
                SNILS_NUMS[posNums],
                INN_NUMS[posNums],
                new java.sql.Date(cal.getTimeInMillis()),
                randomDate(),
                randomDate(),
                randomCardNum(),
                randomCardCode()
            });
            ++posNums;
            if (posNums>=PASS_NUMS.length)
                posNums = 0;
            cal.add(Calendar.YEAR, 1);
            cal.add(Calendar.DAY_OF_YEAR, 5);
            retval.add(new Object[] {
                ++position,
                FIO_FEMALE[i].toUpperCase(),
                "F",
                PASS_NUMS[posNums],
                makePassportDate(cal, posNums),
                SNILS_NUMS[posNums],
                INN_NUMS[posNums],
                new java.sql.Date(cal.getTimeInMillis()),
                randomDate(),
                randomDate(),
                randomCardNum(),
                randomCardCode()
            });
            ++posNums;
            if (posNums>=PASS_NUMS.length)
                posNums = 0;
        }
        return retval;
    }

    private List<MaskingMockColumn> builtTable1() {
        List<MaskingMockColumn> cols = new ArrayList<>();
        cols.add(new MaskingMockColumn(1, "rid", null));
        cols.add(new MaskingMockColumn(2, "nm1_first", dcs(MockDCS.NAME_FIRST, MockDCS.GROUP_A)));
        cols.add(new MaskingMockColumn(3, "nm1_middle", dcs(MockDCS.NAME_MIDDLE, MockDCS.GROUP_A)));
        cols.add(new MaskingMockColumn(4, "nm1_last", dcs(MockDCS.NAME_LAST, MockDCS.GROUP_A)));
        cols.add(new MaskingMockColumn(5, "nm1_sex", dcs(MockDCS.SEX, MockDCS.GROUP_A)));
        cols.add(new MaskingMockColumn(6, "date_birth1", dcs(MockDCS.DOB)));
        cols.add(new MaskingMockColumn(7, "date_added1", null));
        cols.add(new MaskingMockColumn(8, "date_updated1", null));
        cols.add(new MaskingMockColumn(9, "nm2_first", dcs(MockDCS.NAME_FIRST, MockDCS.GROUP_B)));
        cols.add(new MaskingMockColumn(10, "nm2_middle", dcs(MockDCS.NAME_MIDDLE, MockDCS.GROUP_B)));
        cols.add(new MaskingMockColumn(11, "nm2_last", dcs(MockDCS.NAME_LAST, MockDCS.GROUP_B)));
        cols.add(new MaskingMockColumn(12, "nm2_sex", dcs(MockDCS.SEX, MockDCS.GROUP_B)));
        cols.add(new MaskingMockColumn(13, "date_birth2", dcs(MockDCS.DOB)));
        cols.add(new MaskingMockColumn(14, "date_added2", null));
        cols.add(new MaskingMockColumn(15, "date_updated2", null));
        cols.add(new MaskingMockColumn(16, "comment", dcs(MockDCS.PDN_TEXT)));
        return cols;
    }

    private List<Object[]> dataTable1() {
        int maxPos = FIO_MALE.length;
        if (maxPos > FIO_FEMALE.length)
            maxPos = FIO_FEMALE.length;
        int position = 0;
        Calendar cal = Calendar.getInstance();
        cal.set(1975, 0, 1);
        final List<Object[]> retval = new ArrayList<>();
        for (int i=0; i<maxPos; ++i) {
            cal.add(Calendar.YEAR, 1);
            cal.add(Calendar.DAY_OF_YEAR, 5);
            String[] name1 = FIO_MALE[i].toUpperCase().split("[ ]");
            String[] name2 = FIO_FEMALE[maxPos-(i+1)].toUpperCase().split("[ ]");
            retval.add(new Object[] {
                ++position,
                name1[1], name1[2], name1[0], "M",
                new java.sql.Date(cal.getTimeInMillis()),
                randomDate(),
                randomDate(),
                name2[1], name2[2], name2[0], "F",
                new java.sql.Date(cal.getTimeInMillis()),
                randomDate(),
                randomDate(),
                "record #" + position
            });
            cal.add(Calendar.YEAR, 1);
            cal.add(Calendar.DAY_OF_YEAR, 5);
            name1 = FIO_FEMALE[i].toLowerCase().split("[ ]");
            name2 = FIO_MALE[maxPos-(i+1)].toLowerCase().split("[ ]");
            retval.add(new Object[] {
                ++position,
                name1[1], name1[2], name1[0], "F",
                new java.sql.Date(cal.getTimeInMillis()),
                randomDate(),
                randomDate(),
                name2[1], name2[2], name2[0], "M",
                new java.sql.Date(cal.getTimeInMillis()),
                randomDate(),
                randomDate(),
                "record #" + position
            });
        }
        return retval;
    }

    private List<MaskingMockColumn> buildTableAny() {
        List<MaskingMockColumn> cols = new ArrayList<>();
        cols.add(new MaskingMockColumn(1, "a", null));
        cols.add(new MaskingMockColumn(2, "b", dcs(MockDCS.DOB)));
        cols.add(new MaskingMockColumn(3, "c", dcs(MockDCS.INN)));
        cols.add(new MaskingMockColumn(4, "d", dcs(MockDCS.SNILS)));
        cols.add(new MaskingMockColumn(5, "e", dcs(MockDCS.NAME_FULL)));
        return cols;
    }

    private List<Object[]> dataDefault() {
        final List<Object[]> retval = new ArrayList<>();
        retval.add(new Object[] { 1, "2", 3, "4", 5 });
        return retval;
    }

    private java.sql.Timestamp randomDate() {
        return new java.sql.Timestamp(System.currentTimeMillis()
                - ThreadLocalRandom.current()
                .nextLong(31536000000L, 315360000000L) );
    }

    private static final String[] FIO_MALE = new String[] {
        "Царёв Свирид Арсенович",
        "Чапаев Авенир Степанович",
        "Збукарев Ювеналий Иванович",
        "Косоруков Адриан Акакиевич",
        "Гущин Аскольд Арсенович",
        "Милорадов Савватей Арсенович",
        "Мизенов Савёл Иванович",
        "Зыченков Нефёд Степанович",
        "Кулагин Флорентин Арсенович",
        "Носов Люциан Акакиевич",
        "Цапин Иустин Арсенович",
        "Дегтярев Ефимий Арсенович",
        "Краев Георгий Арсенович",
        "Ковалевский Рюрик Арсенович",
        "Приходько Юлий Степанович",
        "Воронцов Дионисий Арсенович",
        "Черкашин Ульян Иванович",
        "Клоков Карп Арсенович",
        "Писчанский Эдуард Арсенович",
        "Задорнов Созонтий Акакиевич"
    };

    private static final String[] FIO_FEMALE = new String[] {
        "Сёмина Софья Пантелеевна",
        "Ломоносова Генриетта Петровна",
        "Искусных Алла Семёновна",
        "Царёва Еликонида Григорьевна",
        "Ефимова Дина Педосиевна",
        "Маркова Марина Андреевна",
        "Кострецкая Станислава Степановна",
        "Победоносцева Розалина Акакиена",
        "Рыжова Эдита Пантелеевна",
        "Ручкина Руфь Петровна",
        "Дежнёва Виргиния Семёновна",
        "Соболевская Яна Григорьевна",
        "Садыкова Александра Педосиевна",
        "Афанасьева Варвара Андреевна",
        "Чаюкова Павлина Степановна",
        "Завражнова Феодотия Акакиена",
        "Николаева Марьяна Пантелеевна",
        "Лаверченко Владимира Петровна",
        "Пересторонина Евлалия Семёновна",
        "Наумкина Мелитина Григорьевна"
    };

    // PASS_NUMS, SNILS_NUMS and INN_NUMS should have the same length.
    // Typically the re-use of their values can be used to check
    // the repeatability of masking algorithms applied.

    private static final String[] PASS_NUMS = new String[] {
        "1569 874596",
        "2341 645677",
        "3468 579472",
        "4892 286485",
        "5522 156621",
        "6365 884596",
        "7341 645677",
        "8468 579472",
        "9892 286485",
        "1022 156621",
    };

    private static final String[] SNILS_NUMS = new String[] {
        "112-233-445 95",
        "212-233-445 03",
        "312-233-445 12",
        "412-233-445 21",
        "512-233-445 30",
        "612-233-445 39",
        "712-233-445 48",
        "812-233-445 57",
        "912-233-445 66",
        "102-233-445 87",
    };

    private static final String[] INN_NUMS = new String[] {
        "190504234914",
        "210504234930",
        "370504224972",
        "410504234968",
        "540504234984",
        "690504234932",
        "730504234990",
        "870504234928",
        "910504234986",
        "100504234956",
    };

    private String randomCardNum() {
        long baseNum = 1234567890123400L;
        // baseNum += ThreadLocalRandom.current().nextInt(10);
        baseNum += Counter.COUNTER.get().next() * 10;
        return Long.toString(baseNum);
    }

    private String randomCardCode() {
        return randomCodes.generate(5, 10);
    }

    private static final class Counter {
        private int value = 0;

        public static final ThreadLocal<Counter> COUNTER
                = ThreadLocal.withInitial(() -> new Counter());

        public int next() {
            if (value > 4) {
                value = 0;
            }
            return value++;
        }
    }

}
