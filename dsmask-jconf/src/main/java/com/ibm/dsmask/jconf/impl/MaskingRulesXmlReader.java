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
package com.ibm.dsmask.jconf.impl;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import com.ibm.dsmask.beans.*;
import com.ibm.dsmask.jconf.beans.*;

/**
 * XML configuration reader for masking rules.
 * @author zinal
 */
public class MaskingRulesXmlReader {

    /**
     * Read the masking rules from an XML DOM tree
     * @param start XML DOM tree root element
     * @return Masking rule registry
     * @throws Exception
     */
    public MaskingRuleRegistry read(Element start) throws Exception {
        // collect all keys
        final Map<String, MaskingKey> keys = new HashMap<>();
        for (Element el : start.getChildren("key")) {
            MaskingKey key = readKey(el);
            if (key==null) {
                throw new Exception("Incomplete key: " + el
                    + ", attrs = " + el.getAttributes());
            }
            if ( keys.put(key.getName(), key) != null ) {
                throw new Exception("Duplicate key name ["
                        + key.getName() + "]");
            }
        }
        // collect all functions
        final Map<String, MaskingFunction> functions = new HashMap<>();
        for (Element el : start.getChildren("function")) {
            MaskingFunction fun = readFunction(el);
            if (fun==null || !fun.isValid()) {
                throw new Exception("Incomplete function: " + el
                    + ", attrs = " + el.getAttributes());
            }
            if ( functions.put(fun.getName(), fun) != null ) {
                throw new Exception("Duplicate function name ["
                        + fun.getName() + "]");
            }
        }
        // collect all rules (which depend on functions)
        final Map<String, MaskingRule> rules = new HashMap<>();
        for (Element el : start.getChildren("rule")) {
            MaskingRule rule = readRule(el, functions);
            if (rule==null || !rule.isValid()) {
                throw new Exception("Incomplete rule: " + el
                    + ", attrs = " + el.getAttributes());
            }
            if (rules.put(rule.getName(), rule)!=null) {
                throw new Exception("Duplicate rule name ["
                        + rule.getName() + "]");
            }
        }
        // build and validate the registry
        return new MaskingRuleRegistry(keys.values(),
                functions.values(), rules.values());
    }

    public MaskingRuleRegistry read(InputStream is) throws Exception {
        return read(new SAXBuilder().build(is).getRootElement());
    }

    public MaskingRuleRegistry read(File f) throws Exception {
        return read(new SAXBuilder().build(f).getRootElement());
    }

    public MaskingRuleRegistry read(String pathname) throws Exception {
        return read(new SAXBuilder().build(pathname).getRootElement());
    }

    /**
     * Construct the rule registry from multiple XML files
     * stored in the directory hierarchy.
     * @param dirname Path to the directory
     * @return Masking rule registry
     * @throws Exception
     */
    public MaskingRuleRegistry readBulk(String dirname) throws Exception {
        // Find the names of files to be loaded
        final List<String> files = new ArrayList<>();
        Files.walk(Paths.get(dirname)).filter(Files::isRegularFile)
                .forEach((f) -> {
                    String fname = f.toString();
                    if (fname.endsWith(".xml"))
                        files.add(fname);
                });
        if (files.isEmpty())
            throw new Exception("No config files in directory " + dirname);
        // Construct the XML DOM tree containing the content from all the files
        org.jdom2.Element root = new org.jdom2.Element("dsmask-masking-rules");
        for (String fname : files) {
            org.jdom2.Element cur = new SAXBuilder().build(fname)
                    .detachRootElement();
            for ( org.jdom2.Element item :
                    new ArrayList<>(cur.getChildren()) ) {
                root.addContent(item.detach());
            }
        }
        // Convert the XML tag soup into a masking rule registry beans
        return read(root);
    }

    private MaskingKey readKey(Element start) throws Exception {
        final MaskingKey key = new MaskingKey (
                start.getAttributeValue("name"),
                start.getAttributeValue("value")
        );
        return key;
    }

    private MaskingFunction readFunction(Element start) throws Exception {
        final MaskingFunction fun = new MaskingFunction (
                start.getAttributeValue("name"),
                FunctionType.fromCode(start.getAttributeValue("type"))
        );
        Element text = start.getChild("text");
        if (text==null) {
            fun.setText(start.getTextTrim());
        } else {
            fun.setText(text.getTextTrim());
            Element textInput = start.getChild("text-input");
            if (textInput!=null)
                fun.setTextInput(textInput.getTextTrim());
        }
        return fun;
    }

    private MaskingRule readRule(Element start,
            Map<String, MaskingFunction> functions) throws Exception {
        final MaskingRule rule = new MaskingRule (
                start.getAttributeValue("name")
        );
        for (Element el : start.getChildren()) {
            if ("field".equalsIgnoreCase(el.getName())) {
                // fields are both arguments and outputs
                rule.addField(getAttrDc(el));
            } else if ("input".equalsIgnoreCase(el.getName())) {
                rule.addArgument(getAttrDc(el));
            } else if ("output".equalsIgnoreCase(el.getName())) {
                rule.addOutput(getAttrDc(el));
            } else if ("step".equalsIgnoreCase(el.getName())) {
                MaskingStep step = readStep(el, functions);
                if (step==null || !step.isValid()) {
                    throw new Exception("Incomplete step: " + el
                        + ", attrs = " + el.getAttributes());
                }
                rule.addStep(step);
            }
        }
        return rule;
    }

    private MaskingStep readStep(Element start,
            Map<String, MaskingFunction> functions) throws Exception {
        String name = Utils.lower(start.getAttributeValue("name"));
        if (name.length()==0)
            name = "#";
        String fun = Utils.lower(getAttrFun(start));
        final MaskingFunction function = functions.get(fun);
        if (function==null) {
            throw new Exception("Illegal function reference [" + fun + "]");
        }
        final MaskingStep step = new MaskingStep(name, function);
        for (Element el : start.getChildren("ref")) {
            String xname = Utils.lower(el.getAttributeValue("name"));
            int position = getAttrPos(el);
            step.addReference(xname, position);
        }
        Element elPred = start.getChild("predicate");
        if (elPred!=null) {
            final MaskingPredicate pred = new MaskingPredicate();
            pred.setText(elPred.getTextTrim());
            step.setPredicate(pred);
            Element elPredInput = start.getChild("predicate-input");
            if (elPredInput!=null)
                pred.setInput(elPredInput.getTextTrim());
        }
        Element elUniq = start.getChild("uniq-check");
        if (elUniq!=null) {
            String providerName = elUniq.getAttributeValue("provider");
            if (providerName==null || providerName.length()==0)
                providerName = "sys$uniq";
            MaskingUniq ut = new MaskingUniq(providerName);
            ut.setInputPositions(grabIndexes(name, elUniq.getChildren("uniq-input")));
            ut.setOutputPositions(grabIndexes(name, elUniq.getChildren("uniq-output")));
            step.setUniqCheck(ut);
        }
        return step;
    }

    private static int[] grabIndexes(String stepName, List<Element> vals) throws Exception {
        if (vals==null || vals.isEmpty())
            return null;
        int[] inputIndexes = new int[vals.size()];
        for (int i=0; i<vals.size(); ++i) {
            String index = vals.get(i).getAttributeValue("position");
            if (index==null)
                index = vals.get(i).getAttributeValue("pos");
            if (index==null)
                index = vals.get(i).getAttributeValue("index");
            if (index==null)
                index = vals.get(i).getAttributeValue("ix");
            if (index==null)
                index = "1";
            final int ix;
            try {
                ix = Integer.parseInt(index) - 1;
            } catch(NumberFormatException nfe) {
                throw new Exception("Bad index format [" + index
                        + "] in uniq-check of step [" + stepName + "]", nfe);
            }
            if (ix < 0 || ix > 1000) {
                throw new Exception("Bad index value [" + index
                        + "] in uniq-check of step [" + stepName + "]");
            }
            inputIndexes[i] = ix;
        }
        return inputIndexes;
    }

    private static String getAttrDc(Element el) {
        String dc = el.getAttributeValue("dc");
        if (dc==null)
            dc = el.getAttributeValue("dcname");
        return dc;
    }

    private static String getAttrFun(Element el) {
        String dc = el.getAttributeValue("fun");
        if (dc==null)
            dc = el.getAttributeValue("function");
        return dc;
    }

    private static int getAttrPos(Element el) {
        String pos = el.getAttributeValue("pos");
        if (pos==null)
            pos = el.getAttributeValue("position");
        return Integer.valueOf(pos);
    }
}
