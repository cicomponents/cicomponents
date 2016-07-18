/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.cicomponents.git.impl;

import lombok.Getter;
import lombok.SneakyThrows;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import org.cicomponents.fs.WorkingDirectory;
import org.cicomponents.git.GitRevision;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.events.RefsChangedEvent;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;

import java.io.File;
import java.util.Dictionary;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public abstract class AbstractLocalGitMonitor extends AbstractGitMonitor {
    @Getter
    protected final Git git;
    private WorkingDirectory workingDirectory;

    @SneakyThrows
    public AbstractLocalGitMonitor(Environment environment, Dictionary<String, ?> dictionary) {
        setEnvironment(environment);
        workingDirectory = getEnvironment().getWorkingDirectoryProvider().getDirectory();
        git = Git.cloneRepository()
                 .setURI(((String) dictionary.get("repository")))
                 .setDirectory(new File(workingDirectory.getDirectory() + "/git"))
                 .call();
        git.getRepository().getListenerList().addRefsChangedListener(this::emitRevisionIfNecessary);
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleWithFixedDelay(this::pull, 0, 10, TimeUnit.SECONDS);
    }

    protected abstract void emitRevisionIfNecessary(RefsChangedEvent event);

    @SneakyThrows
    protected void pull() {
        synchronized (git) {
            git.pull().setRebase(true).call();
            RefsChangedEvent event = new RefsChangedEvent();
            event.setRepository(git.getRepository());
            emitRevisionIfNecessary(event);
        }
    }

    protected WorkingDirectory getWorkingDirectory() {
        return getEnvironment().getWorkingDirectoryProvider().getDirectory();
    }

    protected static class LocalGitRevision implements GitRevision {

        private final Git clone;
        private final ObjectId objectId;
        private final WorkingDirectory workingDirectory;
        private AtomicInteger counter;

        public LocalGitRevision(Git clone, ObjectId objectId, WorkingDirectory workingDirectory) {
            this.clone = clone;
            this.objectId = objectId;
            this.workingDirectory = workingDirectory;
            counter = new AtomicInteger(0);
        }

        @Synchronized("counter")
        @Override public void acquire() {
            counter.incrementAndGet();
        }

        @Override public Git getRepository() {
            return clone;
        }

        @Override public String getPath() {
            return clone.getRepository().getDirectory().getPath();
        }

        @Override public ObjectId getRef() {
            return objectId;
        }

        @Synchronized("counter")
        @Override public void close() {
            if (counter.decrementAndGet() == 0) {
                log.info("Cleaning up {}", clone);
                clone.close();
                workingDirectory.close();
            }
        }
    }
}
