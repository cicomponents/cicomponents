/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.cicomponents.git.impl;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.directory.shared.ldap.model.filter.*;
import org.cicomponents.common.Filter;
import org.cicomponents.git.GitRevisionEmitter;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.hooks.service.FindHook;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.*;
import java.util.function.BiFunction;

@Component(immediate = true)
@Slf4j
public class GitEmitterProvider implements FindHook {

    private ComponentContext context;

    @Reference
    protected volatile Environment environment;

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
            if (objectClass.contentEquals(GitRevisionEmitter.class.getName())) {
                for (Map.Entry<String, Class<? extends GitRevisionEmitter>> entry : emitters.entrySet()) {
                    String type = f.getAttribute("type");
                    if (type.contentEquals(entry.getKey())) {
                        // need to register new service
                        registerService(filter, f, entry, type);
                    }
                }
            }
        }
    }

    private void registerService(String filter, final Filter f,
                                 final Map.Entry<String, Class<? extends GitRevisionEmitter>> entry,
                                 final String type) {
        String repository = f.getAttribute("repository");
        if (repository != null) {
            Thread thread = new Thread() {
                @Override public void run() {
                    Dictionary<String, String> properties = new Hashtable<>();
                    properties.put("type", type);
                    properties.put("repository", repository);
                    String branch = f.getAttribute("branch");
                    properties.put("branch", branch == null ? "master" : branch);
                    @SuppressWarnings("unchecked")
                    Class<GitRevisionEmitter> emitterClass = (Class<GitRevisionEmitter>) entry
                            .getValue();

                    try {
                        GitRevisionEmitter emitter = instantiators.get(emitterClass).apply(environment,
                                                                                           properties);
                        context.getBundleContext()
                               .registerService(GitRevisionEmitter.class, emitter, properties);
                    } catch (Exception e) {
                        log.error("Error while registering {}", emitterClass);
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

    private static Map<String, Class<? extends GitRevisionEmitter>> emitters = new HashMap<>();
    private static Map<Class<? extends GitRevisionEmitter>,
            BiFunction<Environment, Dictionary<String, ?>, ? extends GitRevisionEmitter>> instantiators =
            new HashMap<>();

    static {
        emitters.put("latest", LatestRevisionGitBranchMonitor.class);
        instantiators.put(LatestRevisionGitBranchMonitor.class, LatestRevisionGitBranchMonitor::new);
    }



}
