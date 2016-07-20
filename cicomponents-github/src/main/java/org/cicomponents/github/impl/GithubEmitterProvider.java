/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.cicomponents.github.impl;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.cicomponents.common.Filter;
import org.cicomponents.github.GithubPullRequestEmitter;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.hooks.service.FindHook;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.Collection;
import java.util.Dictionary;
import java.util.Hashtable;

@Component(immediate = true)
@Slf4j
public class GithubEmitterProvider implements FindHook {

    @Reference
    protected volatile Environment environment;

    private ComponentContext context;

    @Activate
    protected void activate(ComponentContext context) {
        this.context = context;
    }

    @SneakyThrows
    @Override public void find(BundleContext bundleContext, String name,
                               String filter, boolean allServices,
                               Collection<ServiceReference<?>> references) {
        if (references.isEmpty()) {
            // possibly need to register new services
            Filter f = new Filter(filter);
            String objectClass = f.getAttribute("objectClass");
            if (objectClass.contentEquals(GithubPullRequestEmitter.class.getName())) {
                // need to register new service
                registerService(filter, f);
            }
        }
    }

    private void registerService(String filter, final Filter f) {
        String repository = f.getAttribute("repository");
        String githubRepository = f.getAttribute("github-repository");
        if (repository == null && githubRepository != null) {
            repository = "https://github.com/" + githubRepository;
        }
        if (repository != null) {
            String finalRepository = repository;
            Thread thread = new Thread() {
                @Override public void run() {
                    Dictionary<String, String> properties = new Hashtable<>();
                    properties.put("repository", finalRepository);
                    properties.put("github-repository", normalizeRepository(finalRepository));
                    try {
                        GithubPullRequestEmitter emitter = new PullRequestMonitor(environment, properties);
                        context.getBundleContext()
                               .registerService(GithubPullRequestEmitter.class, emitter, properties);
                    } catch (Exception e) {
                        log.error("Error while registering GithubPullRequestEmitter");
                        log.error("Error: ", e);
                    }
                }
            };
            thread.setName(repository);
            thread.start();
        } else {
            log.info("Can't find `repository` in GitRepositoryEmitter filter {}", filter);
        }
    }

    private String normalizeRepository(String repository) {
        return repository
                .replaceAll("^https://github\\.com/", "")
                .replaceAll("^git://github\\.com/", "")
                .replaceAll("^github\\.com/", "")
                .replaceAll("\\.git$","");
    }


}
