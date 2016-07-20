/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.cicomponents.git.impl;

import lombok.SneakyThrows;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import org.cicomponents.ResourceHolder;
import org.cicomponents.common.SimpleResourceHolder;
import org.cicomponents.fs.WorkingDirectory;
import org.cicomponents.git.GitRevision;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.events.RefsChangedEvent;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;

import java.io.File;
import java.io.IOException;
import java.util.Dictionary;

@Slf4j
public class LatestRevisionGitBranchMonitor extends AbstractLocalGitMonitor {

    private ObjectId head;
    private final String branch;

    @SneakyThrows
    public LatestRevisionGitBranchMonitor(Environment environment, Dictionary<String, ?> dictionary) {
        super(environment, dictionary);
        branch = (String) dictionary.get("branch");
        checkLatest();
    }

    private void checkLatest() throws IOException {
        synchronized (git) {
            String headRefName = git.getRepository().findRef("refs/heads/" + branch).getObjectId().getName();
            String knownHead = (String) getEnvironment().getPersistentMap().get(headRefName);
            if (knownHead != null) {
                head = ObjectId.fromString(knownHead);
            }
            RefsChangedEvent event = new RefsChangedEvent();
            event.setRepository(git.getRepository());
            git.getRepository().fireEvent(event);
        }
    }

    @SneakyThrows
    protected void emitRevisionIfNecessary(RefsChangedEvent event) {
        synchronized (git) {
            ObjectId newHead = event.getRepository().findRef("refs/heads/" + branch).getObjectId();
            if (!newHead.equals(head)) {
                log.info("Detected refs change for {}, branch {}, old: {}, new: {}", git, branch, head, newHead);
                WorkingDirectory workingDirectory = getWorkingDirectory();
                String directory = workingDirectory.getDirectory() + "/git";
                Git clone = Git.cloneRepository()
                               .setURI("file://" + git.getRepository().getDirectory().getAbsolutePath())
                               .setDirectory(new File(directory))
                               .call();
                Ref checkedOutRef = clone.checkout().setName(newHead.getName()).call();
                assert checkedOutRef == newHead;
                GitRevision resource = new LocalGitRevision(clone, newHead, workingDirectory);
                ResourceHolder<GitRevision> holder = new SimpleResourceHolder<>(resource);
                emit(holder);
                head = newHead;
                getEnvironment().getPersistentMap().put(head.getName(), head.getName());
            }
        }
    }
}
