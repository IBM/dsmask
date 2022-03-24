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

import java.io.IOException;
import java.io.OutputStream;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.jdom2.Document;
import net.dsmask.model.*;

/**
 * Compact output of model entities to the specified output stream.
 * @author zinal
 */
public class XmlDeployerStream implements XmlDeployer, AutoCloseable {

    private final OutputStream os;
    private final Element rootElement;

    public XmlDeployerStream(OutputStream os) throws IOException {
        this.os = os;
        rootElement = new Element(XmlNames.TAG_Root);
    }

    @Override
    public void save(ModelName mn, Element el) {
        rootElement.addContent(el);
    }

    @Override
    public void close() throws IOException {
        new XMLOutputter(Format.getRawFormat())
                .output(new Document(rootElement), os);
        os.flush();
        os.close();
    }

}
