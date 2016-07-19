/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.cicomponents.github;

import org.cicomponents.git.GitRevision;
import org.kohsuke.github.GHPullRequest;

public interface GithubPullRequest extends GitRevision {
    GHPullRequest getPullRequest();
}
