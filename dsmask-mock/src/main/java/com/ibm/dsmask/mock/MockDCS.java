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

/**
 * Mock data classes.
 * Data class codes should be changed in sync with test masking rules.
 * @author zinal
 */
public enum MockDCS {
    CODE("Code", "N"),
    DATE("DateRus", "N"), // non-confidential date
    SEX("GEN", "N"), // gender
    DOB("DOB", "C"), // date of birth
    NAME_FULL("NameFull_Rus", "C"),
    NAME_FIRST("NameFirst_Rus", "C"),
    NAME_MIDDLE("NameMiddle_Rus", "C"),
    NAME_LAST("NameLast_Rus", "C"),
    NAME_LAT("NameFull_Lat", "C"),
    NAME_INIT("NameInit_Rus", "C"),
    PASS_NUM("NID_Dom_Rus", "C"),
    PASS_FOR("NID_Frn_Rus", "C"),
    PASS_DATE("DocDate_Rus", "C"),
    INN("INN_Rus", "C"),
    SNILS("SNILS_Rus", "C"),
    PHONE("PN", "C"),
    EMAIL("EA", "C"), // email address
    CARD_PAN("CardPan", "C"),
    CARD_CODE("CardCode", "C"),
    PDN_TEXT("PersonalDataText", "C"),
    SUPER_CODE("TestSuperCode", "C"),
    GROUP_A("grp_any_a", "G"),
    GROUP_B("grp_any_b", "G"),
    GROUP_C("grp_any_c", "G"),
    GROUP_D("grp_any_d", "G");

    public static final String MODE_NONE = "N";
    public static final String MODE_CONFIDENTIAL = "C";
    public static final String MODE_GROUP = "G";

    public final String name;
    public final String mode;

    MockDCS(String name, String mode) {
        this.name = name;
        this.mode = mode;
    }
}
