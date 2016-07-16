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

@Slf4j
@Component(immediate = true, configurationPolicy = ConfigurationPolicy.REQUIRE, scope = ServiceScope.SINGLETON)
public class Build implements ResourceListener<GitRevision> {
    @Reference(policy = ReferencePolicy.STATIC)
    protected volatile GitRevisionEmitter master;

    @Reference
    protected volatile OutputProviderService outputProviderService;

    @Activate
    protected void activate() {
        master.addResourceListener(this);
    }

    @Deactivate
    protected void deactivate() {
        master.removeResourceListener(this);
    }

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
                                .forTasks("dist")
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
