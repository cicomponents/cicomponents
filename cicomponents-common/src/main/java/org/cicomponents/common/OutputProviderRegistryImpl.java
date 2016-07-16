/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.cicomponents.common;

import lombok.Getter;
import org.cicomponents.OutputProvider;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component(immediate = true, scope = ServiceScope.SINGLETON)
public class OutputProviderRegistryImpl implements OutputProviderRegistry {

    @Getter
    private Map<UUID, OutputProvider> providers = new HashMap<>();

    @Override public void register(OutputProvider outputProvider) {
        UUID uuid = UUID.randomUUID();
        providers.put(uuid, outputProvider);
    }
}
