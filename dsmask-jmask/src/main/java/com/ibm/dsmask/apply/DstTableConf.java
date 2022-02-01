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
package com.ibm.dsmask.apply;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.ibm.dsmask.impl.Utils;

/**
 * Destination table configuration for DsApply operator.
 * @author mzinal
 */
public class DstTableConf {

    private String commonName;
    private String destinationName;
    private final List<String> keyFields = new ArrayList<>();
    private final List<String> allFields = new ArrayList<>();
    private final List<String> errors = new ArrayList<>();

    public List<String> getErrors() {
        return errors;
    }

    public boolean isValid() {
        return errors.isEmpty();
    }

    public String getCommonName() {
        return commonName;
    }

    public void setCommonName(String commonName) {
        this.commonName = commonName;
    }

    public String getDestinationName() {
        return destinationName;
    }

    public void setDestinationName(String destinationName) {
        this.destinationName = destinationName;
    }

    public List<String> getKeyFields() {
        return keyFields;
    }

    public List<String> getAllFields() {
        return allFields;
    }

    /**
     * Validate the settings, generating the list of errors.
     * @param prefix Prefix to the errors, typically a configuration name
     */
    private void validate(String prefix) {
        if (commonName==null)
            errors.add(prefix + ": Common table name was not defined");
        if (destinationName==null)
            errors.add(prefix + ": Destination table name was not defined");
        if (keyFields.isEmpty())
            errors.add(prefix + ": Key fields were not specified");
        if (! allFields.containsAll(keyFields)) {
            errors.add(prefix + ": Some of the key fields "
                    + "are not defined in the full list of fields");
        }
    }

    /**
     * Load the table definition from an XML document
     * @param path  Path to configuration directory
     * @param name  Name of a table definition
     * @return Loaded table definition object
     * @throws Exception In case the XML document was not found or
     *      has invalid format
     */
    public static DstTableConf load(String path, String name)
            throws Exception {
        return load(new File(path, "dsapply-" + name + ".xml").getAbsolutePath());
    }

    /**
     * Load the table definition from an XML document
     * @param fileName  Path to the file
     * @return Loaded table definition object
     * @throws Exception In case the XML document was not found or
     *      has invalid format
     */
    public static DstTableConf load(String fileName)
            throws Exception {
        // Read an XML configuration file
        final org.jdom2.Element config;
        try (FileInputStream fis = new FileInputStream(fileName)) {
            config = new org.jdom2.input.SAXBuilder().build(fis)
                    .getRootElement();
        }
        // Build the output object
        DstTableConf retval = new DstTableConf();
        retval.setCommonName(config.getAttributeValue("name"));
        retval.setDestinationName(config.getAttributeValue("target-name"));
        List<org.jdom2.Element> fields = config.getChildren("field");
        if (fields==null)
            fields = Collections.emptyList();
        for (org.jdom2.Element field : fields) {
            String name = field.getAttributeValue("name");
            boolean isKey = Utils.toBoolean(field.getAttributeValue("key"));
            if (name==null)
                name = "";
            else
                name = name.trim();
            if (name.length() > 0) {
                name = name.toLowerCase();
                retval.allFields.add(name);
                if (isKey)
                    retval.keyFields.add(name);
            }
        }
        retval.validate(retval.commonName != null ? retval.commonName : fileName);
        return retval;
    }

    /**
     * Load all dsapply table files from a specified directory
     * @param path Path to the directory
     * @return List of table configurations
     * @throws java.lang.Exception In case of I/O or XML parsing errors
     */
    public static List<DstTableConf> loadAll(String path)
            throws Exception {
        final File[] files = new File(path).listFiles((File f) ->
                f.isFile() && f.canRead()
                && f.getName().startsWith("dsapply-")
                && f.getName().endsWith(".xml"));
        final List<DstTableConf> retval = new ArrayList<>();
        for (File f : files) {
            retval.add(load(f.getAbsolutePath()));
        }
        return retval;
    }

}
