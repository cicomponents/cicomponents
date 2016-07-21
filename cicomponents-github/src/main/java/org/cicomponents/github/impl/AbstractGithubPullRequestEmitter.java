/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.cicomponents.github.impl;

import org.cicomponents.common.AbstractResourceEmitter;
import org.cicomponents.github.GithubPullRequest;
import org.cicomponents.github.GithubPullRequestEmitter;

public abstract class AbstractGithubPullRequestEmitter extends AbstractResourceEmitter<GithubPullRequest>
        implements GithubPullRequestEmitter {

    abstract void pause();
    abstract void resume();
}
