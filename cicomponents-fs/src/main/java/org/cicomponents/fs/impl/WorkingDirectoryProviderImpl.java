/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.cicomponents.fs.impl;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.cicomponents.fs.WorkingDirectory;
import org.cicomponents.fs.WorkingDirectoryProvider;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

@Slf4j
@Component(scope = ServiceScope.SINGLETON, immediate = true)
public class WorkingDirectoryProviderImpl implements WorkingDirectoryProvider {
    @SneakyThrows
    @Override public WorkingDirectory getDirectory() {
        Path path = Files.createTempDirectory("workingdir-");
        return new WorkingDirectory() {
            @Override public String getDirectory() {
                return path.toString();
            }

            @SneakyThrows
            @Override public void release() {
                log.info("Removing working directory {}", path);
                Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                    @Override public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                            throws IOException {
                        Files.delete(file);
                        return FileVisitResult.CONTINUE;
                    }

                    @Override public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                        Files.delete(dir);
                        return FileVisitResult.CONTINUE;
                    }
                });
            }
        };
    }
}
