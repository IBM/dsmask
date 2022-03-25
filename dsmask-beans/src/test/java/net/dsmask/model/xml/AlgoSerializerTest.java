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

import org.junit.Test;
import org.junit.Assert;
import net.dsmask.model.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 *
 * @author zinal
 */
public class AlgoSerializerTest {

    @Test
    public void test1() throws Exception {
        AlgorithmModule input = makeModule();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        AlgoSerializer.writeStream(input, baos);
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        AlgorithmModule output = AlgoSerializer.readStream(bais);
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
