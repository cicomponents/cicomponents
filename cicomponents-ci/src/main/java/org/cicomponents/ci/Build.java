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
import org.cicomponents.git.GitRevisionEmitter;
import org.cicomponents.github.GithubOAuthTokenProvider;
import org.cicomponents.github.GithubPullRequest;
import org.cicomponents.github.GithubPullRequestEmitter;
import org.eclipse.jgit.api.Git;
import org.gradle.tooling.*;
import org.kohsuke.github.GHCommitState;
import org.osgi.service.component.annotations.*;

import java.io.OutputStream;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/**
 * This is a CI Components build orchestration service.
 *
 * This service implements {@link ResourceListener} for {@link GitRevision} to receive Git-related events.
 */
@Slf4j
@Component(immediate = true, scope = ServiceScope.SINGLETON)
public class Build {

    /**
     * Reference to a master branch version emitter. Relies on cicomponents-git's <code>GitEmitterProvider</code>
     * which implements {@link org.osgi.framework.hooks.service.FindHook} to listen for filter inquieries.
     *
     * To override it, run this in the console:
     *
     * <pre>
     * config:edit org.cicomponents.ci.Build
     * config:property-set master.target "(&(type=latest)(repository=https://github.com/yourfork/cicomponents)(branch=master))"
     * config:update
     * </pre>
     *
     * Or edit etc/org.cicomponents.ci.Build.cfg:
     *
     * <pre>
     * master.target = (&(type=latest)(repository=https://github.com/yourfork/cicomponents)(branch=master))
     * </pre>
     *
     */
    @Reference(target = "(&(type=latest)(repository=https://github.com/cicomponents/cicomponents)(branch=master))")
    protected GitRevisionEmitter master;

    /**
     * Reference to a pull request version emitter.
     *
     */
    @Reference(target = "(github-repository=cicomponents/cicomponents)")
    protected GithubPullRequestEmitter pullRequests;

    @Reference(target = "(github-repository=cicomponents/cicomponents)")
    protected GithubOAuthTokenProvider githubOAuthTokenProvider;

    /**
     * This service is used to create outputs
     */
    @Reference
    protected volatile OutputProviderService outputProviderService;

    @Reference
    protected volatile NodeConfiguration configuration;

    private MasterListener masterListener;
    private PullRequestListener prListener;

    @Activate
    protected void activate() {
        // Become an active masterListener
        masterListener = new MasterListener();
        master.addResourceListener(masterListener);
        prListener = new PullRequestListener();
        pullRequests.addResourceListener(prListener);
    }

    private class Builder implements Supplier<Boolean> {
        private final GitRevision resource;

        private Builder(GitRevision resource) {
            this.resource = resource;
        }

        @SneakyThrows
        @Override public Boolean get() {
            Boolean result = false;
            GradleConnector connector = GradleConnector.newConnector();
            Git repository = resource.getRepository();
            log.info("Building {}", repository);
            connector.forProjectDirectory(resource.getRepository().getRepository().getDirectory().getParentFile());
            ProjectConnection connection = null;
            try {
                connection = connector.connect();

                OutputProvider outputProvider = outputProviderService.createOutputProvider();
                OutputStream standardOutput = outputProvider.getStandardOutput();
                OutputStream standardError = outputProvider.getStandardError();
                BuildLauncher buildLauncher =
                        connection
                                .newBuild()
                                .forTasks("check", "dist")
                                .setStandardOutput(standardOutput)
                                .setStandardError(standardError);

                CompletableFuture<Boolean> future = new CompletableFuture<>();
                buildLauncher.run(new ResultHandler<Void>() {
                    @Override public void onComplete(Void result) {
                        future.complete(true);
                    }

                    @Override public void onFailure(GradleConnectionException failure) {
                        future.complete(false);
                    }
                });
                result = future.join();
                standardOutput.close();
                standardError.close();
            } finally {
                if (connection != null) {
                    connection.close();
                }
            }
            return result;
        }
    }
    private class MasterListener implements ResourceListener<GitRevision> {

        /**
         * This method is invoked when a new {@link GitRevision} has been emitted.
         *
         * @param holder
         * @param emitter
         */
        @Override
        @SneakyThrows
        public void onEmittedResource(ResourceHolder<GitRevision> holder, ResourceEmitter<GitRevision> emitter) {
            try (GitRevision resource = holder.acquire()) {
              new Builder(resource).get();
            }
        }
    }

    private class PullRequestListener implements ResourceListener<GithubPullRequest> {

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
                Boolean result = new Builder(resource).get();
                resource.getPullRequest().getRepository()
                        .createCommitStatus(sha,
                                            result ? GHCommitState.SUCCESS : GHCommitState.FAILURE,
                                            configuration.getUrl() + "/ci/" + sha,
                                            result ? "Success" : "Failure", "cicomponents");
            }
        }
    }

}
