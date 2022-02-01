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
package net.dsmask.model.common;

import java.util.ArrayList;
import java.util.List;
import net.dsmask.model.any.*;

/**
 * The collection of masking profiles, for different tables,
 * defined as a model entity.
 * @author zinal
 */
public class MaskingConfig extends EntityBase {

    private final List<MaskingProfile> profiles = new ArrayList<>();
    
    public MaskingConfig(String name) {
        super(EntityType.Config, name);
    }

    public List<MaskingProfile> getProfiles() {
        return profiles;
    }

}
