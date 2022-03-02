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
package net.dsmask.uniq;

import java.io.Serializable;
import net.dsmask.util.DsMaskUtil;

/**
 * The request to the uniq-check service.
 * @author zinal
 */
public class UniqRequest implements Serializable {

    static final long serialVersionUID = 2020071501L;

    public static final String SEP = DsMaskUtil.fromCodepoint(2);

    // iteration number
    private int iteration;
    // source (unmasked) value
    private String source;
    // current target (masked) value
    private String target;
    // initial (iteration==0) masked value for conflict handling
    private String conflict;

    public UniqRequest() {
        this.iteration = 0;
    }

    public UniqRequest(int iteration) {
        this.iteration = iteration;
    }

    public void clear() {
        iteration = 0;
        source = null;
        target = null;
        conflict = null;
    }

    public int getIteration() {
        return iteration;
    }

    public void setIteration(int iteration) {
        this.iteration = iteration;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public void setSource(Object[] source) {
        this.source = pack(source);
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public void setTarget(Object[] target) {
        this.target = pack(target);
    }

    public String getConflict() {
        return conflict;
    }

    public void setConflict(String conflict) {
        this.conflict = conflict;
    }

    public void setConflict(Object[] conflict) {
        this.conflict = pack(conflict);
    }

    public static String pack(Object[] data) {
        if (data==null || data.length==0)
            return null;
        final StringBuilder sb = new StringBuilder();
        for (Object o : data) {
            if (o!=null)
                sb.append(o.toString());
            sb.append(SEP);
        }
        sb.append(data.length);
        return sb.toString();
    }

}
