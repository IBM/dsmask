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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import net.dsmask.model.*;

/**
 * In-memory model loader with non-indexed retrieveRules() implementation.
 * @author zinal
 */
public class ModelLoaderMemory extends ModelLoaderBase {

    public ModelLoaderMemory(XmlProvider provider) {
        super(provider);
    }

    @Override
    public Collection<? extends AnyRule> retrieveRules(String context,
            Collection<? extends AnyLabel> labels) {
        final List<AnyRule> rules = new ArrayList<>();
        context = ModelUtils.lower(context);
        // List and check all masking rules available
        for (ModelName mn : list(EntityType.Rule)) {
            AnyRule rule = (AnyRule) find(mn);
            if (! matchContext(context, rule))
                continue;
            if (! matchLabels(labels, rule))
                continue;
            rules.add(rule);
        }
        return rules;
    }

    private static boolean matchContext(String context, AnyRule rule) {
        if (rule.getContexts().isEmpty()) {
            // Missing enabled contexts means that the rule
            // is for the default context only.
            return context.length() == 0;
        }
        for (String x : rule.getContexts()) {
            if (context.equals(ModelUtils.lower(x)))
                return true;
        }
        return false;
    }

    private static boolean matchLabels(Collection<? extends AnyLabel> labels,
            AnyRule rule) {
        for (AnyLabel l1 : labels) {
            for (AnyReference r : rule.getInputs()) {
                for (AnyLabel l2 : r.getLabels()) {
                    if (l1.equals(l2)) {
                        // Any match on input means we should process the rule.
                        return true;
                    }
                }
            }
        }
        // No matches, skip the rule.
        return false;
    }

}
