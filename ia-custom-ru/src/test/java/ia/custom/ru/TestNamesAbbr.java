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

import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author zinal
 */
public class TestNamesAbbr {

    @Test
    public void test() {
        TestDicts.setup();

        final NamesAbbr na = new NamesAbbr();
        String in;
        boolean out;

        in = "Пагосян Гагик Спартакович-оглы";
        out = na.matchValue(in);
        Assert.assertEquals(in, false, out);

        in = "Иван Иванов";
        out = na.matchValue(in);
        Assert.assertEquals(in, false, out);

        in = "Петров П. П.";
        out = na.matchValue(in);
        Assert.assertEquals(in, true, out);

        in = "П. Петров";
        out = na.matchValue(in);
        Assert.assertEquals(in, true, out);

        in = "Петров";
        out = na.matchValue(in);
        Assert.assertEquals(in, false, out);

        in = "Г. С.-оглы Пагосян";
        out = na.matchValue(in);
        Assert.assertEquals(in, true, out);

        in = "Служил Гаврила программистом";
        out = na.matchValue(in);
        Assert.assertEquals(in, false, out);
    }

}
