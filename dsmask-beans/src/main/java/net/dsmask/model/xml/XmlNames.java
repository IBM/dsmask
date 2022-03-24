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
package net.dsmask.model.xml;

/**
 * Names of all entities and their attributes.
 * Enforces the ordered use of names, allows to search for the actual usage.
 * @author zinal
 */
public class XmlNames {

    public static final String PROP_PACKAGE_DATA = "01-dsmask-data.xml";

    public static final String VAL_T = "true";
    public static final String VAL_F = "false";

    public static final String ATT_NAME = "name";
    public static final String ATT_POS = "pos";
    public static final String ATT_VALUE = "value";
    public static final String ATT_DEFVAL = "defval";
    public static final String ATT_TYPE = "type";
    public static final String ATT_MODE = "mode";
    public static final String ATT_ALGO = "algo";
    public static final String ATT_PROV = "provider";
    public static final String ATT_FUNC = "function";
    public static final String ATT_FRAGM = "fragment";
    public static final String ATT_ITER = "iterable";
    public static final String ATT_DB = "db";
    public static final String ATT_SCHEMA = "schema";
    public static final String ATT_TABLE = "table";
    public static final String ATT_CI = "ci";
    public static final String ATT_SOURCE = "src";
    public static final String ATT_TARGET = "dst";
    public static final String ATT_IS_RX = "is-rx";
    public static final String ATT_RULE = "rule";
    public static final String ATT_FIELD = "rule";

    public static final String TAG_Root = "dsmask-model";
    public static final String TAG_Fragment = "fragment";
    public static final String TAG_Function = "function";
    public static final String TAG_Key = "init-key";
    public static final String TAG_Rule = "rule";
    public static final String TAG_Label = "label";
    public static final String TAG_Selector = "selector";
    public static final String TAG_Metadata = "metadata";
    public static final String TAG_Profile = "profile";

    public static final String TAG_FuncParam = "func-param";
    public static final String TAG_TableInfo = "table-info";
    public static final String TAG_Field = "field";
    public static final String TAG_FieldLabel = "field-label";
    public static final String TAG_FieldTag = "field-tag";
    public static final String TAG_ItemSequence = "item-seq";
    public static final String TAG_ItemMeta = "item-meta";
    public static final String TAG_ItemPred = "item-pred";
    public static final String TAG_ItemArg = "item-arg";
    public static final String TAG_Uniq = "uniq-check";
    public static final String TAG_UniqIn = "uniq-in";
    public static final String TAG_UniqOut = "uniq-out";
    public static final String TAG_ItemFunc = "item-function";
    public static final String TAG_ItemScript = "item-script";
    public static final String TAG_ItemBlock = "item-block";
    public static final String TAG_ItemFragment = "item-fragment";
    public static final String TAG_ScriptBody = "script-body";
    public static final String TAG_RuleMeta = "rule-meta";
    public static final String TAG_RuleIn = "rule-in";
    public static final String TAG_RuleOut = "rule-out";
    public static final String TAG_RuleLabel = "rule-label";
    public static final String TAG_RuleCtx = "rule-ctx";
    public static final String TAG_SelectorItem = "selector-item";
    public static final String TAG_Operation = "profile-op";
    public static final String TAG_OperationIn = "op-in";
    public static final String TAG_OperationOut = "op-out";

}
