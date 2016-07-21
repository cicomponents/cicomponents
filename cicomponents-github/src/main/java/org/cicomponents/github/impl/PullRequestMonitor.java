/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.cicomponents.github.impl;

import com.google.common.collect.Iterables;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import org.cicomponents.common.SimpleResourceHolder;
import org.cicomponents.fs.WorkingDirectory;
import org.cicomponents.github.GithubOAuthTokenProvider;
import org.cicomponents.github.GithubPullRequest;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.ObjectId;
import org.kohsuke.github.*;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

import java.io.File;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
public class PullRequestMonitor extends AbstractGithubPullRequestEmitter {

    public static final String HOOK_NAME = "web";
    public static final String TYPE = "pull-request-v1";
    public static final String CI_COMPONENTS_TYPE = "ci_components_type";
    private final BundleContext context;
    @Getter
    private final String repository;
    private final Environment environment;
    private final Map<String, Object> persistentMap;

    private volatile GithubOAuthTokenProvider tokenProvider;
    private final ServiceTracker<GithubOAuthTokenProvider, Boolean> serviceTracker;

    public PullRequestMonitor(Environment environment, Dictionary<String, ?> dictionary) {
        this.environment = environment;
        persistentMap = environment.getPersistentMap();
        context = FrameworkUtil.getBundle(PullRequestMonitor.class).getBundleContext();
        repository = (String) dictionary.get("github-repository");

        serviceTracker = new ServiceTracker<>(context, GithubOAuthTokenProvider.class,
                                              new GithubOAuthTokenProviderServiceTracker());
    }

    @Override void resume() {
        serviceTracker.open();
    }

    @Override void pause() {
        serviceTracker.close();
    }

    @SneakyThrows
    private void ensureHookIsPresent() {
        GitHub gitHub = GitHub.connectUsingOAuth(tokenProvider.getAccessToken());
        GHRepository repository = gitHub.getRepository(this.repository);
        boolean hookPresent = Iterables.any(repository.getHooks(),
                                            hook -> hook.getConfig()
                                                        .getOrDefault(CI_COMPONENTS_TYPE, "")
                                                        .contentEquals(TYPE));
        if (!hookPresent) {
            log.info("No pull request hook present for {}, creating one", this.repository);
            Map<String, String> props = new HashMap<>();
            props.put(CI_COMPONENTS_TYPE, TYPE);
            props.put("url", environment.getConfiguration().getUrl() + GithubPRWebhookServlet.PATH);
            props.put("content_type", "json");
            repository.createHook(HOOK_NAME, props,
                                  Arrays.asList(GHEvent.PULL_REQUEST), true);
        }
    }


    @Getter
    private Map<Integer, GHPullRequest> openPullRequests = new HashMap<>();

    @SneakyThrows
    private void checkOpenPullRequests() {
        GitHub gitHub = GitHub.connectUsingOAuth(tokenProvider.getAccessToken());
        GHRepository repo = gitHub.getRepository(this.repository);
        openPullRequests =
                repo.getPullRequests(GHIssueState.OPEN).stream()
                    .collect(Collectors.toMap(GHPullRequest::getNumber, Function.identity()));

        openPullRequests.values().forEach(this::checkPullRequest);
    }

    private void checkPullRequest(GHPullRequest pr) {
        Object o = persistentMap.get(getIssueStatusKey(pr));
        if (o == null) { // no status available for it
            log.info("No status available for {}#{}-{}", repository,
                     pr.getNumber(), pr.getHead().getSha());
            emit(pr);
        }
    }

    @SneakyThrows
    @Override public void onPullRequestEvent(int number) {
        log.info("Pull request event received for {}#{}", this.repository, number);
        GitHub gitHub = GitHub.connectUsingOAuth(tokenProvider.getAccessToken());
        GHRepository repo = gitHub.getRepository(this.repository);
        GHPullRequest pr = repo.getPullRequest(number);
        if (pr.getState() == GHIssueState.OPEN) {
            openPullRequests.put(number, pr);
            checkPullRequest(pr);
        } else {
            openPullRequests.remove(number);
        }
    }

    @SneakyThrows
    private void emit(GHPullRequest pr) {
        WorkingDirectory directory = environment.getWorkingDirectoryProvider().getDirectory();
        String path = directory.getDirectory() + "/git";
        String name = pr.getHead().getRepository().getFullName();
        log.info("Cloning fork {}#{}", name, pr.getHead().getSha());
        Git git = Git.cloneRepository()
                     .setURI("https://github.com/" + name)
                     .setDirectory(new File(path)).call();

        git.checkout().setName(pr.getHead().getSha()).call();
        log.info("Emitting fork {}#{}", name, pr.getHead().getSha());

        GithubPullRequest pullRequest = new GithubPullRequestResource(git, pr, directory);
        persistentMap.put(getIssueStatusKey(pr), new Date());
        emit(new SimpleResourceHolder<>(pullRequest));
    }

    private String getIssueStatusKey(GHPullRequest pr) {
        return "github-pr-status-" +
                     repository.replaceAll("/", "-") + "-" + pr.getNumber() + "-" +
                     pr.getHead().getSha();
    }

    private static class GithubPullRequestResource implements GithubPullRequest {

        private final Git git;
        private final GHPullRequest pr;
        private final WorkingDirectory workingDirectory;
        private final AtomicInteger counter = new AtomicInteger(0);

        public GithubPullRequestResource(Git git, GHPullRequest pr,
                                         WorkingDirectory workingDirectory) {
            this.git = git;
            this.pr = pr;
            this.workingDirectory = workingDirectory;
        }

        @Synchronized("counter")
        @Override public void acquire() {
            counter.incrementAndGet();
        }

        @Override public Git getRepository() {
            return git;
        }

        @Override public ObjectId getRef() {
            return ObjectId.fromString(pr.getHead().getSha());
        }

        @Override public GHPullRequest getPullRequest() {
            return pr;
        }

        @Override public String getPath() {
            return git.getRepository().getDirectory().getPath();
        }

        @Synchronized("counter")
        @Override public void release() {
            if (counter.decrementAndGet() == 0) {
                log.info("Cleaning up {}", git);
                git.close();
                workingDirectory.close();
            }
        }
    }

    private class GithubOAuthTokenProviderServiceTracker implements
            ServiceTrackerCustomizer<GithubOAuthTokenProvider, Boolean> {
        @Override
        public Boolean addingService(ServiceReference<GithubOAuthTokenProvider> serviceReference) {
            if (serviceReference.getProperty("github-repository").equals(repository)) {
                GithubOAuthTokenProvider service = context.getService(serviceReference);
                tokenProvider = service;
                ensureHookIsPresent();
                checkOpenPullRequests();
                return true;
            } else {
                return false;
            }
        }

        @Override
        public void modifiedService(ServiceReference<GithubOAuthTokenProvider> serviceReference,
                                    Boolean o) {

        }

        @Override
        public void removedService(ServiceReference<GithubOAuthTokenProvider> serviceReference,
                                   Boolean o) {
        }
    }
}
