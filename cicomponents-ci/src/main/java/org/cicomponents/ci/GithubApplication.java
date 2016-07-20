/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.cicomponents.ci;

import lombok.Getter;
import org.cicomponents.github.GithubApplicationCredentialsProvider;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.ServiceScope;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This component provides GitHub application credentials for CI Component's repositories
 *
 * To configure it, use this in console:
 *
 * <pre>
 * config:edit org.cicomponents.ci.GithubApplication
 * config:property-set clientId ...
 * config:property-set clientSecret ...
 * config:property-set repositories cicomponents/cicomponents, eventsourcing/es4j
 * config:update
 * </pre>
 *
 * or edit etc/org.cicomponents.ci.GithubApplication.cfg:
 *
 * <pre>
 * clientId = ...
 * clientSecret = ...
 * repositories = cicomponents/cicomponents, eventsourcing/es4j
 * </pre>
 *
 */
@Component(scope = ServiceScope.SINGLETON, configurationPolicy= ConfigurationPolicy.REQUIRE, immediate = true)
public class GithubApplication implements GithubApplicationCredentialsProvider {

    @Getter
    private String clientId;
    @Getter
    private String clientSecret;
    @Getter
    private List<String> repositories;

    @Activate
    protected void activate(GithubApplicationConfiguration configuration) {
        clientId = configuration.clientId();
        clientSecret = configuration.clientSecret();
        repositories = Arrays.stream(configuration.repositories().split(","))
                             .map(String::trim).collect(Collectors.toList());
    }
}
