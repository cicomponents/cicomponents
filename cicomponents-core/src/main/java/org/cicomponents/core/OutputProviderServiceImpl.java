/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.cicomponents.core;

import org.cicomponents.OutputProvider;
import org.cicomponents.OutputProviderRegistry;
import org.cicomponents.OutputProviderService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;

@Component(scope = ServiceScope.SINGLETON)
public class OutputProviderServiceImpl implements OutputProviderService {
    @Reference
    protected volatile OutputProviderRegistry registry;

    @Override public OutputProvider createOutputProvider() {
        OutputProviderImpl outputProvider = new OutputProviderImpl();
        registry.register(outputProvider);
        return outputProvider;
    }
}
