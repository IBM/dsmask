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
package com.ibm.dsmask.ops;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringTokenizer;
import org.apache.commons.text.WordUtils;
import com.ibm.dsmask.beans.MskFunc;
import net.dsmask.algo.CyrillicTranslit;

/**
 *
 * @author zinal
 */
public class AlStringOp implements AlSimpleValue {

    private final List<Op> operations = new ArrayList<>();

    public AlStringOp(MskFunc function) {
        this.parse(function.getText());
    }

    private void parse(String config) {
        String[] lines = config.split("[\n\r]+");
        for (String sline : lines) {
            String line = sline.trim();
            if (line.length()==0)
                continue;
            // Parse operation line
            final String[] tokens = new StringTokenizer(line).getTokenArray();
            if (tokens.length==0)
                continue;
            unquote(tokens);
            // Identify operation type
            OpType opType = null;
            for (OpType ot : OpType.values()) {
                if (ot.name().equalsIgnoreCase(tokens[0].trim())) {
                    opType = ot;
                    break;
                }
            }
            if (opType==null) {
                throw new RuntimeException("Unsupported string operation: ["
                        + tokens[0] + "]");
            }
            final Op op;
            switch (opType) {
                case LPad:
                case RPad:
                    if (tokens.length == 2) {
                        op = new OpPad(opType, tokens[1], null);
                    } else if (tokens.length == 3) {
                        op = new OpPad(opType, tokens[1], tokens[2]);
                    } else {
                        throw new RuntimeException("Operation " + opType
                                + " requires 1 or 2 arguments, " + (tokens.length-1)
                                + " specified");
                    }
                    break;
                case Replace:
                    if (tokens.length != 3) {
                        throw new RuntimeException("Operation " + opType
                                + " requires 2 arguments, " + (tokens.length-1)
                                + " specified");
                    }
                    op = new OpRepl(tokens[1], tokens[2]);
                    break;
                default:
                    op = new Op(opType);
            }
            operations.add(op);
        }
    }

    private void unquote(String[] tokens) {
        for (int i=0; i<tokens.length; ++i) {
            String s = tokens[i];
            if (s.startsWith("\"") && s.endsWith("\"") && s.length()>1) {
                s = s.substring(1, s.length()-1);
            }
            s = s.trim();
            tokens[i] = s;
        }
    }

    @Override
    public Object exec(Object in) {
        if (in==null)
            return null;
        String val = in.toString();
        for (Op op : operations) {
            val = exec(op, val);
        }
        return val;
    }

    private String exec(Op op, String val) {
        if (val==null)
            return null;
        switch (op.type) {
            case Lower:
                return val.toLowerCase();
            case Upper:
                return val.toUpperCase();
            case Trim:
                return val.trim();
            case LTrim:
                return StringUtils.stripStart(val, null);
            case RTrim:
                return StringUtils.stripEnd(val, null);
            case LPad:
            case RPad:
                return execPad((OpPad)op, val);
            case SpaceNorm:
                return StringUtils.normalizeSpace(val);
            case Capitalize:
                return WordUtils.capitalizeFully(val);
            case Replace:
                return execReplace((OpRepl)op, val);
            case Translit:
                return CyrillicTranslit.map(val);
        }
        throw new IllegalArgumentException("Unsupported operation: " + op.type);
    }

    private String execReplace(OpRepl op, String val) {
        return op.pattern.matcher(val).replaceAll(op.replacement);
    }

    private String execPad(OpPad op, String val) {
        if (OpType.LPad.equals(op.type))
            return StringUtils.leftPad(val, op.count, op.what);
        else
            return StringUtils.rightPad(val, op.count, op.what);
    }

    @Override
    public boolean isIterationsSupported() {
        return false;
    }

    @Override
    public Object exec(Object in, int iteration) {
        return exec(in);
    }

    public static enum OpType {
        Lower,
        Upper,
        Trim,
        LTrim,
        RTrim,
        LPad,
        RPad,
        SpaceNorm,
        Capitalize,
        Replace,
        Translit
    }

    public static class Op {
        final OpType type;
        public Op(OpType type) {
            this.type = type;
        }
    }
    public static class OpRepl extends Op {
        final Pattern pattern;
        final String replacement;
        public OpRepl(String rx, String repl) {
            super(OpType.Replace);
            this.pattern = Pattern.compile(rx);
            this.replacement = repl;
        }
    }
    public static class OpPad extends Op {
        final int count;
        final String what;
        public OpPad(OpType opType, String count, String what) {
            super(opType);
            try {
                this.count = Integer.parseInt(count);
            } catch(NumberFormatException nfe) {
                throw new AlgoInitException("StringOp/OpPad: Invalid value "
                        + "for number of characters: [" + count + "]", nfe);
            }
            this.what = what;
        }
    }

}
