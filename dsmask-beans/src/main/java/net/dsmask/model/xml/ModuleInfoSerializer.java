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
package net.dsmask.model.xml;

import org.jdom2.Element;
import net.dsmask.model.*;

/**
 * XML in/out for @AlgorithmModule
 * @author zinal
 */
public class ModuleInfoSerializer {

    public static final String TAG_AlgoModule = "algo-module";
    public static final String TAG_AlgoInfo = "algo-info";
    public static final String TAG_AlgoParam = "algo-param";
    public static final String TAG_AlgoItem = "algo-item";

    public static final String ATT_name = "name";
    public static final String ATT_iter = "iter";
    public static final String ATT_type = "type";
    public static final String ATT_defval = "defval";
    public static final String ATT_value = "value";

    public static AlgorithmModule readJdom(Element elModule) {
        if (! TAG_AlgoModule.equalsIgnoreCase(elModule.getName())) {
            throw new IllegalArgumentException("Illegal XML element name: "
                    + elModule.getName());
        }
        final AlgorithmModule am;
        am = new AlgorithmModule(XmlObject.getAttr(elModule, ATT_name));
        for (Element elAlgo : elModule.getChildren(TAG_AlgoInfo)) {
            AlgorithmInfo ai = new AlgorithmInfo(
                    XmlObject.getAttr(elAlgo, ATT_name),
                    XmlObject.getBool(elAlgo, ATT_iter));
            for (Element elParam : elAlgo.getChildren(TAG_AlgoParam)) {
                AlgorithmParameter ap = new AlgorithmParameter(
                        XmlObject.getAttr(elParam, ATT_name),
                        ParameterType.fromString(
                                XmlObject.getAttr(elParam, ATT_type)
                        ),
                        XmlObject.getAttr(elParam, ATT_defval, null));
                for (Element elItem : elParam.getChildren(TAG_AlgoItem)) {
                    ap.addItem(XmlObject.getAttr(elItem, ATT_value));
                }
                ai.addParameter(ap);
            }
            am.registerAlgorithm(ai);
        }
        return am;
    }

    public static Element writeJdom(AlgorithmModule am) {
        Element elModule = new Element(TAG_AlgoModule);
        elModule.setAttribute(ATT_name, am.getModuleName());
        for (AlgorithmInfo ai : am.getAlgorithms()) {
            Element elAlgo = new Element(TAG_AlgoInfo);
            elAlgo.setAttribute(ATT_name, ai.getName());
            elAlgo.setAttribute(ATT_iter,
                    ai.isIterable() ? XmlNames.VAL_T : XmlNames.VAL_F);
            for (AlgorithmParameter ap : ai.getParameters()) {
                Element elParam = new Element(TAG_AlgoParam);
                elParam.setAttribute(ATT_name, ap.getName());
                elParam.setAttribute(ATT_type, ap.getType().name());
                if (ap.getDefval()!=null && ap.getDefval().length() > 0)
                    elParam.setAttribute(ATT_defval, ap.getDefval());
                for (String item : ap.getItems()) {
                    Element elItem = new Element(TAG_AlgoItem);
                    elItem.setAttribute(ATT_value, item);
                    elParam.addContent(elItem);
                }
                elAlgo.addContent(elParam);
            }
            elModule.addContent(elAlgo);
        }
        return elModule;
    }

}
