/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.cicomponents.core;

import lombok.Getter;
import org.cicomponents.NodeConfiguration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.ServiceScope;

@Component(configurationPolicy = ConfigurationPolicy.REQUIRE,
           immediate = true, scope = ServiceScope.SINGLETON)
public class Configuration implements NodeConfiguration {

    @Getter
    private String url;

    @Activate
    protected void activate(ConfigurationProperties config) {
        url = config.url();
    }
}
