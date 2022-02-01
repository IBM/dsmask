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
package com.ibm.dsmask.jconf.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import com.ibm.dsmask.jconf.beans.*;

/**
 *
 * @author zinal
 */
public class RuleSelector {

    private static final org.slf4j.Logger LOG = Utils.logger(RuleSelector.class);

    private DataClassRegistry dataClassRegistry;
    private MaskingRuleRegistry ruleRegistry;

    public DataClassRegistry getDataClassRegistry() {
        return dataClassRegistry;
    }

    public void setDataClassRegistry(DataClassRegistry dataClassRegistry) {
        this.dataClassRegistry = dataClassRegistry;
    }

    public MaskingRuleRegistry getRuleRegistry() {
        return ruleRegistry;
    }

    public void setRuleRegistry(MaskingRuleRegistry ruleRegistry) {
        this.ruleRegistry = ruleRegistry;
    }

    /**
     * Choose the proper set of masking rules to be applied,
     * provide the masking profile on output.
     * @param table Table to be masked
     * @param context Processing context (normally null or empty string)
     * @return Masking profile
     */
    public MaskingProfile select(TableInfo table, String context) {
        context = Utils.lower(context);
        // minimal masking profile contains just reference to table
        final MaskingProfile profile = new MaskingProfile();
        profile.setTableInfo(table);
        // Prepare the set of rules
        final RuleSet ruleSet = new RuleSet().prepare(table, context);
        if (!ruleSet.isReady())
            return profile; // nothing to mask - no usable rules
        // build the map of field groups, and sort by group size, largest first
        final List<FieldGroup> groups = new ArrayList<>
                (buildGroups(table).values());
        Collections.sort(groups, new GroupSizeComparator());
        // for each generated group, prepare the matching others-group
        final List<FieldGroup> others = new ArrayList<>();
        for (FieldGroup group : groups)
            others.add(buildOther(table, group));
        // append the last-chance group, containing all the fields
        groups.add(buildAll(table));
        others.add(buildNone()); // this last group has no "others"
        // for each group, apply all complex rules, because
        // we prefer complex rules over trivial rules
        final List<MaskingOperation> complexOps = new ArrayList<>();
        for (MaskingRule rule : ruleSet.complexRules) {
            for (int i=0; i<groups.size(); ++i) {
                final MaskingOperation tmo =
                        matchComplexRule(rule, groups.get(i), others.get(i));
                if (tmo!=null)
                    complexOps.add(tmo);
            }
        }
        if (complexOps.isEmpty()) {
            // if we have no matching complex rules, we just apply
            //   all simple rules available and return
            final List<MaskingOperation> trivialOps = new ArrayList<>();
            for (FieldInfo fi : ruleSet.confidentialFields) {
                for (String dcname : fi.getConfClasses(dataClassRegistry)) {
                    MaskingRule rule = ruleSet.trivialRules.get(dcname);
                    if (rule!=null) {
                        MaskingOperation tmo = new MaskingOperation();
                        tmo.setMaskingRule(rule);
                        tmo.setArguments(Collections.singletonList(fi));
                        tmo.setOutputs(Collections.singletonList(fi));
                        trivialOps.add(tmo);
                        break; // skip other data classes
                    }
                }
            }
            profile.setOperations(trivialOps);
            return profile;
        }
        // We got a set of complex masking operations.
        // They can overlap, e.g. have the same fields in the output.
        // Overlapping is prohibited for obvious reasons,
        // and we have different options to handle this by excluding
        // the overlapping operations.
        List<MaskingOperation> bestConfig = null;
        int unhandledFields = table.getFields().size();
        for (List<MaskingOperation> currentOps :
                new ComplexOpsGenerator(complexOps).run().convert()) {
            int currentUnhandled = 0;
            final List<MaskingOperation> trivialOps = new ArrayList<>();
            for (FieldInfo fi : ruleSet.confidentialFields) {
                boolean handled = isFieldHandled(fi, currentOps);
                if (!handled) {
                    // The field must be handled by trivial masking rule.
                    for (String dcname : fi.getConfClasses(dataClassRegistry)) {
                        MaskingRule rule = ruleSet.trivialRules.get(dcname);
                        if (rule!=null) {
                            MaskingOperation tmo = new MaskingOperation();
                            tmo.setMaskingRule(rule);
                            tmo.setArguments(Collections.singletonList(fi));
                            tmo.setOutputs(Collections.singletonList(fi));
                            trivialOps.add(tmo);
                            handled = true;
                            break; // skip other data classes
                        }
                    }
                }
                if (!handled) {
                    ++currentUnhandled; // cannot handle the current field
                }
            }
            if (bestConfig==null || unhandledFields>currentUnhandled) {
                // we can enhance the best configuration
                bestConfig = new ArrayList<>();
                bestConfig.addAll(currentOps);
                bestConfig.addAll(trivialOps);
                unhandledFields = currentUnhandled;
            }
        }
        if (bestConfig==null)
            profile.setOperations(Collections.emptyList());
        else
            profile.setOperations(bestConfig);
        return profile;
    }

    /**
     * Validate the masking profile - ensure that all confidential fields
     * are masked by the masking operations, and that there are no
     * intersections between the masking operations.
     * @param profile Profile to be validated
     * @return true if validation successful, false otherwise
     */
    public boolean validate(MaskingProfile profile) {
        // TODO: implementation
        return true;
    }

    /**
     * Exclude masking rules which cannot be applied to a table
     * because they require at least one missing data class.
     * @param rules The full set of masking rules
     * @param table Table definition
     * @return Filtered list of masking rules
     */
    private List<MaskingRule> filterByClasses(List<MaskingRule> rules,
            TableInfo table) {
        if (rules==null || rules.isEmpty())
            return Collections.emptyList();
        final Set<String> dataClasses = table.getAllDataClasses();
        if (dataClasses.isEmpty())
            return Collections.emptyList();
        final ArrayList<MaskingRule> retval = new ArrayList<>();
        for (MaskingRule mr : rules) {
            if (allIn(mr.getArguments(), dataClasses)
                    && allIn(mr.getOutputs(), dataClasses))
                retval.add(mr);
        }
        return retval;
    }

    /**
     * Are all entries contained in a set specified
     * @param entries Some collection of entries
     * @param set The set, against which entries are tested
     * @return true, if all entries are in the set, and false otherwise
     */
    private static boolean allIn(Collection<RuleArgument> entries,
            Set<String> set) {
        if (entries.isEmpty())
            return false;
        for (RuleArgument e : entries) {
            boolean found = false;
            for (String dcname : e.getNames()) {
                if (set.contains(dcname)) {
                    // one of the allowed data classes found
                    found = true;
                    break;
                }
            }
            if (! found)
                return false;
        }
        return true;
    }

    /**
     * Sort the masking rules by the number of arguments.
     * The idea is that more specific masking rules take precedence.
     * @param rules List of masking rules
     * @return Sorted list of masking rules
     */
    private List<MaskingRule> sortByArgs(List<MaskingRule> rules) {
        final ArrayList<MaskingRule> retval = new ArrayList<>(rules);
        Collections.sort(retval, new Comparator<MaskingRule>() {
            @Override
            public int compare(MaskingRule o1, MaskingRule o2) {
                int s1 = (o1==null) ? 0 : o1.getArguments().size();
                int s2 = (o2==null) ? 0 : o2.getArguments().size();
                if (s1==s2) {
                    s1 = (o1==null) ? 0 : o1.getOutputs().size();
                    s2 = (o2==null) ? 0 : o2.getOutputs().size();
                }
                return (s2 - s1);
            }
        });
        return retval;
    }

    /**
     * Build the map of field groups, grouping fields by data classes
     * @param table The reference to table information
     * @return map: grouping data class to list of fields
     */
    private Map<String, FieldGroup> buildGroups(TableInfo table) {
        final Map<String, FieldGroup> retval = new TreeMap<>();
        // Split all fields by grouping classes
        // (each field can go to multiple groups)
        for (FieldInfo fi : table.getFields()) {
            for (String dcname : fi.getDcs()) {
                DataClass dcref = dataClassRegistry.find(dcname);
                if (dcref!=null && dcref.isGroup()) {
                    FieldGroup cur = retval.get(dcname);
                    if (cur==null) {
                        cur = new FieldGroup(dcname);
                        retval.put(dcname, cur);
                    }
                    cur.add(fi);
                }
            }
        }
        // We will skip 1-element groups and non-confidential groups
        final Set<String> excludedKeys = new HashSet<>();
        for (Map.Entry<String, FieldGroup> me : retval.entrySet()) {
            if (me.getValue().getFields().size() < 2) {
                // groups of only 1 element are of no interest to us
                excludedKeys.add(me.getKey());
            }
            boolean confidential = false;
            for (FieldInfo fi : me.getValue().getFields()) {
                if (fi.isConfidential(dataClassRegistry)) {
                    // we have at least one confidential field in a group
                    confidential = true;
                    break;
                }
            }
            if (!confidential) {
                // we do not need groups without confidential fields
                excludedKeys.add(me.getKey());
            }
        }
        // exclude non-interesting groups
        for (String key : excludedKeys)
            retval.remove(key);
        return retval;
    }

    /**
     * Build the group of fields not included in the specified group.
     * @param table Table and field info
     * @param group Current group
     * @return New group, consisting of fields not included in parameter
     */
    private FieldGroup buildOther(TableInfo table, FieldGroup group) {
        final FieldGroup retval = new FieldGroup("- " + group.getIdentity());
        for (FieldInfo fi : table.getFields()) {
            if (!group.getFields().contains(fi))
                retval.add(fi);
        }
        return retval;
    }

    /**
     * Build the group of all fields in a table
     * @param table Table and field info
     * @return New group, consisting of all fields in a table
     */
    private FieldGroup buildAll(TableInfo table) {
        final FieldGroup retval = new FieldGroup("");
        retval.addAll(table.getFields());
        return retval;
    }

    /**
     * Build the empty field group.
     * @return New group with no fields
     */
    private FieldGroup buildNone() {
        return new FieldGroup("-");
    }

    /**
     * Try to build the masking operation for the rule over
     * the specified group of fields.
     * @param rule Masking rule
     * @param group Group of fields
     * @param other Other fields to be used
     * @return New masking operation, or null, if it cannot be defined
     */
    private MaskingOperation matchComplexRule(MaskingRule rule,
            FieldGroup group, FieldGroup other) {
        final MaskingOperation tmo = new MaskingOperation();
        // Each output value needs to be mapped
        //   to a single field inside the group.
        for (RuleArgument out : rule.getOutputs()) {
            FieldInfo outField = group.findField(out);
            if (outField==null)
                return null; // cannot match the output data class
            tmo.getOutputs().add(outField);
        }
        // Each input argument needs to be found in our group,
        //   or at least among the other (remaining) fields.
        for (RuleArgument in : rule.getArguments()) {
            FieldInfo inField = group.findField(in);
            if (inField==null)
                inField = other.findField(in);
            if (inField==null)
                return null; // cannot match the input argument
            tmo.getArguments().add(inField);
        }
        tmo.setMaskingRule(rule);
        return tmo;
    }

    /**
     * Check if the field is already handled by one of the masking operations.
     * MAYBE: logger rid of this method, replace it by a set of already handled
 fields (for more efficiency).
     * @param fi Field information
     * @param ops Current set of masking operations
     * @return true, if field is already handled, false otherwise
     */
    private boolean isFieldHandled(FieldInfo fi,
            List<MaskingOperation> ops) {
        for (MaskingOperation tmo : ops) {
            if (tmo.isFieldHandled(fi))
                return true;
        }
        return false;
    }

    /**
     * Generate a map of intersections.
     * Each entry maps the position to other intersecting positions.
     * @param ops List of masking operations
     * @return Map of position to list of intersecting positions
     */
    private Map<Integer, List<Integer>> buildIntersections(
            List<MaskingOperation> ops) {
        final Map<Integer, List<Integer>> retval = new HashMap<>();
        for (int i=0; i<ops.size(); ++i) {
            final MaskingOperation tmo1 = ops.get(i);
            List<Integer> entries = null;
            for (int j=0; j<ops.size(); ++j) {
                if (i==j) continue;
                final MaskingOperation tmo2 = ops.get(j);
                if (tmo1.intersects(tmo2)) {
                    if (entries==null)
                        entries = new ArrayList<>();
                    entries.add(j);
                }
            }
            if (entries!=null)
                retval.put(i, entries);
        }
        return retval;
    }

    /**
     * Compare the groups by their sizes
     */
    private static class GroupSizeComparator
            implements Comparator<FieldGroup> {
        @Override
        public int compare(FieldGroup o1, FieldGroup o2) {
            if (o1==o2)
                return 0;
            if (o1==null)
                return 1;
            if (o2==null)
                return -1;
            return o2.getFields().size() - o1.getFields().size();
        }
    } // class GroupSizeComparator

    /**
     * The set of masking rules relevant to the table being processed
     */
    private final class RuleSet {
        private List<MaskingRule> complexRules = null;
        private Map<String, MaskingRule> trivialRules = null;
        private List<FieldInfo> confidentialFields = null;

        boolean isReady() {
            return complexRules!=null && trivialRules!=null
                    && (!complexRules.isEmpty() || !trivialRules.isEmpty());
        }

        RuleSet prepare(TableInfo table, String context) {
            // logger the full set of rules for a specific context
            List<MaskingRule> allRules = ruleRegistry.retrieveRules(context);
            // append the default rules to the end of list
            if (context!=null && context.trim().length()>0) {
                final ArrayList<MaskingRule> temp = new ArrayList<>(allRules);
                temp.addAll(ruleRegistry.retrieveRules(null));
                allRules = temp;
            }
            // exclude non-relevant rules
            // (those for which we have do not have all necessary fields)
            allRules = filterByClasses(allRules, table);
            if (allRules.isEmpty())
                return this; // nothing to mask - no rules available
            // Separate trivial vs complex rules.
            // Trivial rules map a single data class to itself.
            trivialRules = new HashMap<>();
            complexRules = new ArrayList<>();
            for (MaskingRule mr : allRules) {
                if (mr.getArguments().size()==1
                        && mr.getOutputs().size()==1
                        && mr.getArgument(0).equals(mr.getOutput(0))) {
                    for (String dcname : mr.getArgument(0).getNames())
                        trivialRules.put(dcname, mr);
                } else {
                    complexRules.add(mr);
                }
            }
            // MAYBE: exclude possible duplicates in the rules
            // Sort the complex rules by number of arguments,
            // as we prefer more complex rules over the simpler ones.
            complexRules = sortByArgs(complexRules);
            // Retrieve the full set of confidential fields
            confidentialFields = table.getCondidentialFields(dataClassRegistry);
            return this;
        }
    } // class RuleSet

    private final class ComplexOpsGenerator {
        private final List<MaskingOperation> ops;
        private final boolean switches[];
        private final List<boolean[]> result;
        private Map<Integer, List<Integer>> intersections;

        ComplexOpsGenerator(List<MaskingOperation> ops) {
            this.ops = ops;
            this.switches = new boolean[ops.size()];
            this.result = new ArrayList<>();
            this.intersections = null;
        }

        ComplexOpsGenerator run() {
            if (intersections==null)
                intersections = buildIntersections(ops);
            Arrays.fill(switches, false);
            step(0);
            return this;
        }

        List<List<MaskingOperation>> convert() {
            final List<List<MaskingOperation>> retval =
                    new ArrayList<>(result.size());
            for (boolean[] cur : result) {
                retval.add(convert(cur));
            }
            return retval;
        }

        private List<MaskingOperation> convert(boolean[] cur) {
            final List<MaskingOperation> retval = new ArrayList<>();
            for (int i=0; i<cur.length; ++i) {
                if (cur[i])
                    retval.add(ops.get(i));
            }
            return retval;
        }

        private boolean hasAnyTrue() {
            for (boolean b : switches) {
                if (b)
                    return true;
            }
            return false;
        }

        private void step(int position) {
            LOG.debug("COG:step entry {} {}", position, switches);
            // skip (and turn ON) non-intersecting operations
            List<Integer> related = null;
            while (position < switches.length) {
                related = intersections.get(position);
                if (related!=null)
                    break;
                switches[position] = true;
                ++position;
            }
            if (related==null) {
                // exit from the recursion - collect the results
                if (hasAnyTrue()) {
                    // avoid adding all-false states at the end of recursion
                    boolean cur[] = new boolean[switches.length];
                    System.arraycopy(switches, 0, cur, 0, switches.length);
                    result.add(cur);
                    LOG.debug("COG:step added {}", cur);
                }
                return;
            }
            // are we allowed to have ON setting?
            boolean allowOn = true;
            for (Integer r : related) {
                if (r<position && switches[r]) {
                    // the related switch is already ON
                    allowOn = false;
                    break;
                }
            }
            if (allowOn) {
                switches[position] = true;
                step(position+1);
            }
            switches[position] = false;
            step(position+1);
        }

    } // ComplexOpsGenerator

}
