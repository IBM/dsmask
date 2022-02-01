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

import com.ibm.infosphere.classification.ValueBasedClassifier;

/**
 * Почтовый адрес для Российской Федерации.
 * @author zinal
 */
public class PostalAddress implements ValueBasedClassifier {

    private transient DcsDict cities = null;
    private transient DcsDict regions = null;
    private transient DcsDict mapObjects = null;
    private transient DcsDict distObjects = null;
    private transient DcsDict cityObjects = null;
    private transient DcsDict bldObjects = null;

    @Override
    public boolean matchValue(Object value) {
        if (value==null || !(value instanceof String))
            return false;
        String v = value.toString().toLowerCase();
        int counter = 0;
        boolean isCityName = false;
        for (String t : v.split("\\s")) {
            t = DcsDict.normalize(t);
            if (t.length()==0)
                continue;
            if (getMapObjects().containsDirect(t)
                    || getDistObjects().containsDirect(t)
                    || getCityObjects().containsDirect(t)
                    || getBldObjects().containsDirect(t)
                    || getRegions().containsDirect(t)) {
                counter++;
                continue;
            }
            if (!isCityName) {
                if (getCities().contains(t)) {
                    counter ++;
                    isCityName = true;
                }
            }
        }
        return (counter >= 2);
    }

    public DcsDict getCities() {
        if (cities==null)
            cities = DcsFactory.dictionary("Addr_cities");
        return cities;
    }

    public DcsDict getRegions() {
        if (regions==null)
            regions = DcsFactory.dictionary("Addr_regions");
        return regions;
    }

    public DcsDict getMapObjects() {
        if (mapObjects==null)
            mapObjects = DcsFactory.dictionary("Addr_mapObjects");
        return mapObjects;
    }

    public DcsDict getDistObjects() {
        if (distObjects==null)
            distObjects = DcsFactory.dictionary("Addr_distObjects");
        return distObjects;
    }

    public DcsDict getCityObjects() {
        if (cityObjects==null)
            cityObjects = DcsFactory.dictionary("Addr_cityObjects");
        return cityObjects;
    }

    public DcsDict getBldObjects() {
        if (bldObjects==null)
            bldObjects = DcsFactory.dictionary("Addr_bldObjects");
        return bldObjects;
    }

}
