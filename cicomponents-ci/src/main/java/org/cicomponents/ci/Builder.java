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
import org.cicomponents.OutputProvider;
import org.cicomponents.OutputProviderService;
import org.cicomponents.git.GitRevision;
import org.eclipse.jgit.api.Git;
import org.gradle.tooling.*;

import java.io.OutputStream;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

@Slf4j
public class Builder implements Supplier<Boolean> {
    private final GitRevision resource;
    private final OutputProviderService outputProviderService;

    Builder(GitRevision resource, OutputProviderService outputProviderService) {
        this.resource = resource;
        this.outputProviderService = outputProviderService;
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
                            .forTasks("dist", "check")
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
