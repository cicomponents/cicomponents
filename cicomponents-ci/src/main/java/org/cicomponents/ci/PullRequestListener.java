/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.cicomponents.ci;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.cicomponents.*;
import org.cicomponents.git.GitRevision;
import org.cicomponents.github.GithubPullRequest;
import org.cicomponents.github.GithubPullRequestEmitter;
import org.kohsuke.github.GHCommitState;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;

@Slf4j
@Component(property = {"github-repository=cicomponents/cicomponents"})
public class PullRequestListener implements ResourceListener<GithubPullRequest> {

    /**
     * This service is used to create outputs
     */
    @Reference
    protected OutputProviderService outputProviderService;

    @Reference
    protected NodeConfiguration configuration;

    /**
     * This method is invoked when a new {@link GitRevision} has been emitted.
     *
     * @param holder
     * @param emitter
     */
    @Override
    @SneakyThrows
    public void onEmittedResource(ResourceHolder<GithubPullRequest> holder, ResourceEmitter<GithubPullRequest> emitter) {
        try (GithubPullRequest resource = holder.acquire()) {
            log.info("Received pull request " + resource.getPullRequest().getNumber());
            String sha = resource.getPullRequest().getHead().getSha();
            resource.getPullRequest().getRepository()
                    .createCommitStatus(sha,
                                        GHCommitState.PENDING, configuration.getUrl() + "/ci/" + sha,
                                        "Build in progress", "cicomponents");
            Boolean result = new Builder(resource, outputProviderService).get();
            resource.getPullRequest().getRepository()
                    .createCommitStatus(sha,
                                        result ? GHCommitState.SUCCESS : GHCommitState.FAILURE,
                                        configuration.getUrl() + "/ci/" + sha,
                                        result ? "Success" : "Failure", "cicomponents");
        }
    }
}
