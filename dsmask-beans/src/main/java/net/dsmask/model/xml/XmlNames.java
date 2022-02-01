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

    public static String PROP_PACKAGE_NAME = "koodaus.package.name";
    public static String PROP_PACKAGE_INFO = "00-koodaus-package.properties";
    public static String PROP_PACKAGE_DATA = "01-koodaus-package.xml";

    public static String VAL_T = "true";
    public static String VAL_F = "false";

    public static String ATT_NAME = "name";
    public static String ATT_POS = "pos";
    public static String ATT_VALUE = "value";
    public static String ATT_DEFVAL = "defval";
    public static String ATT_TYPE = "type";
    public static String ATT_MODE = "mode";
    public static String ATT_ALGO = "algo";
    public static String ATT_PROV = "provider";
    public static String ATT_FUNC = "function";
    public static String ATT_FRAGM = "fragment";
    public static String ATT_ITER = "iterable";
    public static String ATT_DB = "db";
    public static String ATT_SCHEMA = "schema";
    public static String ATT_TABLE = "table";
    public static String ATT_CI = "ci";
    public static String ATT_SOURCE = "src";
    public static String ATT_TARGET = "dst";
    public static String ATT_IS_RX = "is-rx";
    public static String ATT_RULE = "rule";
    public static String ATT_FIELD = "rule";

    public static String TAG_Root = "koodaus-model";
    public static String TAG_Fragment = "fragment";
    public static String TAG_Function = "function";
    public static String TAG_Key = "init-key";
    public static String TAG_Rule = "rule";
    public static String TAG_Label = "label";
    public static String TAG_Selector = "selector";
    public static String TAG_Metadata = "metadata";
    public static String TAG_Config = "config";
    public static String TAG_Profile = "profile";

    public static String TAG_FuncParam = "func-param";
    public static String TAG_TableInfo = "table-info";
    public static String TAG_Field = "field";
    public static String TAG_FieldLabel = "field-label";
    public static String TAG_FieldTag = "field-tag";
    public static String TAG_ItemSequence = "item-seq";
    public static String TAG_ItemMeta = "item-meta";
    public static String TAG_ItemPred = "item-pred";
    public static String TAG_ItemArg = "item-arg";
    public static String TAG_Uniq = "uniq-check";
    public static String TAG_UniqIn = "uniq-in";
    public static String TAG_UniqOut = "uniq-out";
    public static String TAG_ItemFunc = "item-function";
    public static String TAG_ItemScript = "item-script";
    public static String TAG_ItemBlock = "item-block";
    public static String TAG_ItemFragment = "item-fragment";
    public static String TAG_ScriptBody = "script-body";
    public static String TAG_RuleMeta = "rule-meta";
    public static String TAG_RuleIn = "rule-in";
    public static String TAG_RuleOut = "rule-out";
    public static String TAG_RuleLabel = "rule-label";
    public static String TAG_RuleCtx = "rule-ctx";
    public static String TAG_SelectorItem = "selector-item";
    public static String TAG_Operation = "profile-op";
    public static String TAG_OperationIn = "op-in";
    public static String TAG_OperationOut = "op-out";

}
