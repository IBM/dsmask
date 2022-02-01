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
package com.ibm.dsmask.jconf.beans;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Masking rule argument.
 * The set of data class names which are allowed as the rule argument.
 * @author zinal
 */
public class RuleArgument {

    private final Set<String> dcnames = new HashSet<>();

    public RuleArgument() {
    }

    /**
     * Create rule argument, parsing the list of data class names.
     * @param args Comma-separated list of data class names.
     */
    public RuleArgument(String args) {
        this.addArgs(args);
    }

    public final void addArgs(String args) {
        final String[] xargs = args.split("[,;:]");
        for (String dcname : xargs) {
            final String val = Utils.lower(dcname);
            if (val.length()>0)
                this.dcnames.add(val);
        }
    }

    public void clear() {
        dcnames.clear();
    }

    public void add(String dcname) {
        dcname = Utils.lower(dcname);
        if (dcname.length()>0)
          this.dcnames.add(dcname);
    }

    public Set<String> getNames() {
        return Collections.unmodifiableSet(dcnames);
    }

    public boolean isValid() {
        if (dcnames.isEmpty())
            return false;
        for (String dcname : dcnames) {
            if (dcname==null || dcname.trim().isEmpty())
                return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 73 * hash + Objects.hashCode(this.dcnames);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final RuleArgument other = (RuleArgument) obj;
        if (!Objects.equals(this.dcnames, other.dcnames)) {
            return false;
        }
        return true;
    }

}
