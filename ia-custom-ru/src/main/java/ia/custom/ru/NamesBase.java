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

/**
 * Доступ к справочникам имён, фамилий и отчеств.
 * @author zinal
 */
public abstract class NamesBase {

    private transient DcsDict namesFirst = null;
    private transient DcsDict namesMiddle = null;
    private transient DcsDict namesLast = null;
    private transient DcsDict namesItems = null;

    public final DcsDict getNamesFirst() {
        if (namesFirst == null)
            namesFirst = DcsFactory.dictionary("Names_First");
        return namesFirst;
    }

    public final DcsDict getNamesMiddle() {
        if (namesMiddle == null)
            namesMiddle = DcsFactory.dictionary("Names_Middle");
        return namesMiddle;
    }

    public final DcsDict getNamesLast() {
        if (namesLast == null)
            namesLast = DcsFactory.dictionary("Names_Last");
        return namesLast;
    }

    public final DcsDict getNamesItems() {
        if (namesItems == null)
            namesItems = DcsFactory.dictionary("Names_Items");
        return namesItems;
    }

    /**
     * Нормализация и деление имени на части
     * @param value Входное значение
     * @return Компоненты имени
     */
    public String[] extract(Object value) {
        return DcsDict.normalize(value.toString().replace('-', ' ')).split(" ");
    }

}
