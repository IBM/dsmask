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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import org.junit.Test;
import net.dsmask.model.*;
import org.junit.Assert;

/**
 *
 * @author zinal
 */
public class ModuleInfoSerializerTest {

    @Test
    public void test() throws Exception {
        AlgorithmModule input = makeModule();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ModuleInfoSerializer.writeStream(input, baos);
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        AlgorithmModule output = ModuleInfoSerializer.readStream(bais);
        Assert.assertEquals(input, output);
        //System.out.println(new String(baos.toByteArray(), StandardCharsets.UTF_8));
    }

    private AlgorithmModule makeModule() {
        AlgorithmModule am = new AlgorithmModule("test-module");

        AlgorithmInfo ai;
        AlgorithmParameter ap;

        ai = new AlgorithmInfo("test-algo-1", true);
        ap = new AlgorithmParameter("p1");
        ai.addParameter(ap);
        ap = new AlgorithmParameter("p2", ParameterType.Integer);
        ai.addParameter(ap);
        ap = new AlgorithmParameter("p3", ParameterType.Long, "-1");
        ai.addParameter(ap);
        am.registerAlgorithm(ai);

        ai = new AlgorithmInfo("test-algo-2", false);
        ap = new AlgorithmParameter("x1");
        ai.addParameter(ap);
        ap = new AlgorithmParameter("x2", ParameterType.Text, "");
        ai.addParameter(ap);
        ap = new AlgorithmParameter("x3", ParameterType.Key, "mainkey");
        ai.addParameter(ap);
        ap = new AlgorithmParameter("x4", ParameterType.Item);
        ap.addItem("i1");
        ap.addItem("i2");
        ap.addItem("i3");
        ai.addParameter(ap);
        am.registerAlgorithm(ai);

        return am;
    }

}
