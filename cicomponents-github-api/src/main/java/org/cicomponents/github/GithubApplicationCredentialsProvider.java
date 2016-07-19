/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.cicomponents.github;

import java.util.List;

/**
 * Provider of credentials for a set of repositories ({@link #getRepositories()})
 */
public interface GithubApplicationCredentialsProvider {
    /**
     * Client ID
     * @return
     */
    String getClientId();

    /**
     * Client secret
     * @return
     */
    String getClientSecret();

    /**
     * List of repositories in a user/repo format, for example <code>{"cicomponents/cicomponents",
     * "eventsourcing/es4j"}</code>
     * @return
     */
    List<String> getRepositories();
}
