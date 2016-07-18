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
import org.eclipse.jgit.api.Git;
import org.gradle.tooling.*;
import org.osgi.service.component.annotations.*;

import java.io.OutputStream;
import java.util.concurrent.CompletableFuture;

/**
 * This is a CI Components build orchestration service.
 *
 * This service implements {@link ResourceListener} for {@link GitRevision} to receive Git-related events.
 */
@Slf4j
@Component(immediate = true, configurationPolicy = ConfigurationPolicy.REQUIRE, scope = ServiceScope.SINGLETON)
public class Build implements ResourceListener<GitRevision> {

    /**
     * Reference to a master branch version emitter. Relies on cicomponents-git's <code>GitEmitterProvider</code>
     * which implements {@link org.osgi.framework.hooks.service.FindHook} to listen for filter inquieries.
     *
     * To configure it, run this in the console:
     *
     * <pre>
     * config:edit org.cicomponents.ci.Build
     * config:property-set master.target "(&(type=latest)(repository=https://github.com/cicomponents/cicomponents)(branch=master))"
     * config:update
     * </pre>
     *
     * Or edit etc/org.cicomponents.ci.Build.cfg:
     *
     * <pre>
     * master.target = (&(type=latest)(repository=https://github.com/cicomponents/cicomponents)(branch=master))
     * </pre>
     *
     */
    @Reference
    protected volatile GitRevisionEmitter master;

    /**
     * This service is used to create outputs
     */
    @Reference
    protected volatile OutputProviderService outputProviderService;

    @Activate
    protected void activate() {
        // Become an active listener
        master.addResourceListener(this);
    }

    @Deactivate
    protected void deactivate() {
        master.removeResourceListener(this);
    }

    /**
     * This method is invoked when a new {@link GitRevision} has been emitted.
     *
     * @param holder
     * @param emitter Right now we don't check the emitter because there's only one for now.
     */
    @Override
    @SneakyThrows
    public void onEmittedResource(ResourceHolder<GitRevision> holder, ResourceEmitter<GitRevision> emitter) {
        GradleConnector connector = GradleConnector.newConnector();
        try (GitRevision resource = holder.acquire()) {
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
                future.join();
                standardOutput.close();
                standardError.close();
            } finally {
                if (connection != null) {
                    connection.close();
                }
            }
        }
    }
}
