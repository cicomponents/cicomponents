/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.cicomponents.test;

import com.cicomponents.test.ContainerConfiguration;
import lombok.Getter;
import lombok.SneakyThrows;
import org.cicomponents.ResourceEmitter;
import org.cicomponents.ResourceHolder;
import org.cicomponents.ResourceListener;
import org.cicomponents.fs.WorkingDirectory;
import org.cicomponents.fs.WorkingDirectoryProvider;
import org.cicomponents.git.GitRevision;
import org.cicomponents.git.GitRevisionEmitter;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerMethod;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;

import javax.inject.Inject;
import java.io.File;
import java.util.Collection;
import java.util.Hashtable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerMethod.class)
public class GitEmitterTest {

    @Inject
    private BundleContext bundleContext;

    @Configuration
    public Option[] config() {
        return ContainerConfiguration.withDefaultConfig();
    }

    @Test
    @SneakyThrows
    public void noListeners() {
        Collection<ServiceReference<GitRevisionEmitter>> references = bundleContext
                .getServiceReferences(GitRevisionEmitter.class,
                                      "(objectClass=" + GitRevisionEmitter.class.getName() + ")");
        assertTrue(references.isEmpty());
    }

    static class Listener implements ResourceListener<GitRevision> {
        @Getter
        private CompletableFuture<ObjectId> future = new CompletableFuture<>();
        @Override
        public void onEmittedResource(ResourceHolder<GitRevision> holder, ResourceEmitter<GitRevision> emitter) {
            try (GitRevision resource = holder.acquire()) {
                future.complete(resource.getRef());
            }
        }

        void reset() {
            future = new CompletableFuture<>();
        }
    }

    @Inject
    private WorkingDirectoryProvider workingDirectoryProvider;

    @Test
    @SneakyThrows
    public void listenerDiscovery() {
        try (WorkingDirectory directory = workingDirectoryProvider.getDirectory()) {
            Git git = Git.init().setDirectory(new File(directory.getDirectory())).call();
            RevCommit commit = git.commit().setAllowEmpty(true).setMessage("Test").call();

            Hashtable<String, Object> properties = new Hashtable<>();
            properties.put("repository","file://" + git.getRepository().getDirectory().getAbsolutePath());
            properties.put("type", "latest");
            Listener listener = new Listener();
            ServiceRegistration<ResourceListener> registration = bundleContext
                    .registerService(ResourceListener.class, listener, properties);


            Collection<ServiceReference<GitRevisionEmitter>> references = bundleContext
                    .getServiceReferences(GitRevisionEmitter.class,
                                          "(objectClass=" + GitRevisionEmitter.class.getName() + ")");
            assertFalse(references.isEmpty());

            registration.unregister();

            git.close();
        }
    }

    @Test
    @SneakyThrows
    public void listenerRemoval() {
        try (WorkingDirectory directory = workingDirectoryProvider.getDirectory()) {
            Git git = Git.init().setDirectory(new File(directory.getDirectory())).call();
            RevCommit commit = git.commit().setAllowEmpty(true).setMessage("Test").call();

            Hashtable<String, Object> properties = new Hashtable<>();
            properties.put("repository","file://" + git.getRepository().getDirectory().getAbsolutePath());
            properties.put("type", "latest");
            Listener listener = new Listener();
            ServiceRegistration<ResourceListener> registration = bundleContext
                    .registerService(ResourceListener.class, listener, properties);


            Collection<ServiceReference<GitRevisionEmitter>> references = bundleContext
                    .getServiceReferences(GitRevisionEmitter.class,
                                          "(objectClass=" + GitRevisionEmitter.class.getName() + ")");
            assertFalse(references.isEmpty());

            ObjectId objectId = listener.getFuture().get();
            assertEquals(commit.toObjectId(), objectId);
            listener.reset();

            registration.unregister();

            git.commit().setAllowEmpty(true).setMessage("Test #1").call();

            try {
                listener.getFuture().get(11, TimeUnit.SECONDS);
                fail("Listener was not removed");
            } catch (TimeoutException e) {
                // this is what should happen
            }

            git.close();
        }
    }

    @Test
    @SneakyThrows
    public void listener() {
        try (WorkingDirectory directory = workingDirectoryProvider.getDirectory()) {
            Git git = Git.init().setDirectory(new File(directory.getDirectory())).call();
            RevCommit commit = git.commit().setAllowEmpty(true).setMessage("Test").call();

            Hashtable<String, Object> properties = new Hashtable<>();
            properties.put("repository","file://" + git.getRepository().getDirectory().getAbsolutePath());
            properties.put("type", "latest");
            Listener listener = new Listener();
            ServiceRegistration<ResourceListener> registration = bundleContext
                    .registerService(ResourceListener.class, listener, properties);


            Collection<ServiceReference<GitRevisionEmitter>> references = bundleContext
                    .getServiceReferences(GitRevisionEmitter.class,
                                          "(objectClass=" + GitRevisionEmitter.class.getName() + ")");

            ObjectId objectId = listener.getFuture().get();
            assertEquals(commit.toObjectId(), objectId);
            listener.reset();

            // Test ongoing commits
            commit = git.commit().setAllowEmpty(true).setMessage("Test #1").call();

            objectId = listener.getFuture().get();
            assertEquals(commit.toObjectId(), objectId);

            git.close();
            registration.unregister();
        }
    }
}
