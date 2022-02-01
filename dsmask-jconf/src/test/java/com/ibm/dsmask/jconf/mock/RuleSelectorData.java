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
package com.ibm.dsmask.jconf.mock;

import com.ibm.dsmask.mock.MockDCS;
import com.ibm.dsmask.mock.MaskingMockData;
import java.util.ArrayList;
import com.ibm.dsmask.jconf.beans.*;
import com.ibm.dsmask.jconf.impl.*;

/**
 *
 * @author zinal
 */
public class RuleSelectorData {

    private static String[] dcs(MockDCS... vals) {
        if (vals==null || vals.length==0)
            return new String[] {};
        final String[] retval = new String[vals.length];
        for (int i=0; i<vals.length; ++i)
            retval[i] = vals[i].name;
        return retval;
    }

    private static TableInfo convertMockTable(MaskingMockData mock) {
        TableInfo table = new TableInfo();
        table.setDatabase("source");
        table.setName(mock.getTableName());
        mock.getColumns().forEach((col) -> {
            table.addField(new FieldInfo(col.getName(), dcs(col.getDcs())));
        });
        return table;
    }

    public static TableInfo tableGrouping() {
        return convertMockTable(new MaskingMockData(MaskingMockData.TAB1) );
    }

    public static TableInfo tableOverlaps() {
        return convertMockTable(new MaskingMockData(MaskingMockData.TAB2) );
    }

    public static TableInfo tablePrimitive() {
        return convertMockTable(new MaskingMockData(MaskingMockData.TAB3) );
    }

    public static MaskingProfile profileGrouping(TableInfo table,
            MaskingRuleRegistry maskingRuleRegistry) {
        MaskingProfile expected = new MaskingProfile();
        expected.setTableInfo(table);
        expected.setOperations(new ArrayList<>());
        MaskingOperation mop;
        // --
        mop = new MaskingOperation();
        mop.setMaskingRule(maskingRuleRegistry.findRule("name-3part-sex-hash"));
        mop.setArguments(table.findFields("nm1_sex", "nm1_last", "nm1_first", "nm1_middle" ));
        mop.setOutputs(table.findFields("nm1_last", "nm1_first", "nm1_middle"));
        expected.getOperations().add(mop);
        // --
        mop = new MaskingOperation();
        mop.setMaskingRule(maskingRuleRegistry.findRule("name-3part-sex-hash"));
        mop.setArguments(table.findFields("nm2_sex", "nm2_last", "nm2_first", "nm2_middle"));
        mop.setOutputs(table.findFields("nm2_last", "nm2_first", "nm2_middle"));
        expected.getOperations().add(mop);
        // --
        mop = new MaskingOperation();
        mop.setMaskingRule(maskingRuleRegistry.findRule("DOB"));
        mop.setArguments(table.findFields("date_birth1"));
        mop.setOutputs(mop.getArguments());
        expected.getOperations().add(mop);
        // --
        mop = new MaskingOperation();
        mop.setMaskingRule(maskingRuleRegistry.findRule("DOB"));
        mop.setArguments(table.findFields("date_birth2"));
        mop.setOutputs(mop.getArguments());
        expected.getOperations().add(mop);
        // --
        return expected;
    }

    public static MaskingProfile profileOverlaps(TableInfo table,
            MaskingRuleRegistry maskingRuleRegistry) {
        // order of operations is not significant
        MaskingProfile expected = new MaskingProfile();
        expected.setTableInfo(table);
        expected.setOperations(new ArrayList<>());
        MaskingOperation mop;
        // --
        mop = new MaskingOperation();
        mop.setMaskingRule(maskingRuleRegistry.findRule("name-full-sex-hash"));
        mop.setArguments(table.findFields("sex_code", "nm_full"));
        mop.setOutputs(table.findFields("nm_full"));
        expected.getOperations().add(mop);
        // --
        mop = new MaskingOperation();
        mop.setMaskingRule(maskingRuleRegistry.findRule("DOB"));
        mop.setArguments(table.findFields("date_birth"));
        mop.setOutputs(mop.getArguments());
        expected.getOperations().add(mop);
        // --
        mop = new MaskingOperation();
        mop.setMaskingRule(maskingRuleRegistry.findRule("card-pan"));
        mop.setArguments(table.findFields("card_num"));
        mop.setOutputs(mop.getArguments());
        expected.getOperations().add(mop);
        // --
        mop = new MaskingOperation();
        mop.setMaskingRule(maskingRuleRegistry.findRule("card-code"));
        mop.setArguments(table.findFields("card_code"));
        mop.setOutputs(mop.getArguments());
        expected.getOperations().add(mop);
        // --
        mop = new MaskingOperation();
        mop.setMaskingRule(maskingRuleRegistry.findRule("snils"));
        mop.setArguments(table.findFields("doc_snils"));
        mop.setOutputs(mop.getArguments());
        expected.getOperations().add(mop);
        // --
        mop = new MaskingOperation();
        mop.setMaskingRule(maskingRuleRegistry.findRule("passp-single"));
        mop.setArguments(table.findFields("doc_pass_num"));
        mop.setOutputs(mop.getArguments());
        expected.getOperations().add(mop);
        // --
        mop = new MaskingOperation();
        mop.setMaskingRule(maskingRuleRegistry.findRule("DocDate"));
        mop.setArguments(table.findFields("doc_pass_date"));
        mop.setOutputs(mop.getArguments());
        expected.getOperations().add(mop);
        // --
        mop = new MaskingOperation();
        mop.setMaskingRule(maskingRuleRegistry.findRule("INN"));
        mop.setArguments(table.findFields("doc_inn"));
        mop.setOutputs(mop.getArguments());
        expected.getOperations().add(mop);
        // --
        return expected;
    }


    public static MaskingProfile profilePrimitive(TableInfo table,
            MaskingRuleRegistry maskingRuleRegistry) {
        // order of operations is not significant
        MaskingProfile expected = new MaskingProfile();
        expected.setTableInfo(table);
        expected.setOperations(new ArrayList<>());
        MaskingOperation mop;
        // --
        mop = new MaskingOperation();
        mop.setMaskingRule(maskingRuleRegistry.findRule("test-code"));
        mop.setArguments(table.findFields("code"));
        mop.setOutputs(table.findFields("code"));
        expected.getOperations().add(mop);
        // --
        return expected;
    }

}
