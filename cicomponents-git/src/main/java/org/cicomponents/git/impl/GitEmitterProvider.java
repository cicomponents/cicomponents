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
import org.cicomponents.ResourceListener;
import org.cicomponents.git.GitRevision;
import org.cicomponents.git.GitRevisionEmitter;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.*;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Consumer;

@Component(immediate = true, scope = ServiceScope.SINGLETON)
@Slf4j
public class GitEmitterProvider {

    @Reference
    protected Environment environment;

    @Reference(policyOption = ReferencePolicyOption.GREEDY)
    protected Collection<Map.Entry<Map<String, Object>, ResourceListener<GitRevision>>> listeners;

    @SneakyThrows
    private void forEachService(ComponentContext context, Consumer<GitRevisionEmitter> consumer) {
        Collection<ServiceReference<GitRevisionEmitter>> references =
                context.getBundleContext().getServiceReferences(GitRevisionEmitter.class,
                                                                "(objectClass=" + GitRevisionEmitter.class.getName() + ")");
        for (ServiceReference<GitRevisionEmitter> reference : references) {
            GitRevisionEmitter service = context.getBundleContext().getService(reference);
            consumer.accept(service);
            context.getBundleContext().ungetService(reference);
        }
    }

    @SneakyThrows
    @Activate
    protected void activate(ComponentContext context) {
        log.info("Activating with {} potential listener(s)", listeners.size());
        forEachService(context, (emitter) -> {
            if (emitter instanceof AbstractGitMonitor) {
                ((AbstractGitMonitor) emitter).pause();
            }
        });
        for (Map.Entry<Map<String, Object>, ResourceListener<GitRevision>> listener : listeners) {
            if (listener.getValue().isMatchingListener(GitRevision.class)) {
                Map<String, Object> props = listener.getKey();
                String filter = getEmitterFilter(props);
                Collection<ServiceReference<GitRevisionEmitter>> references =
                        context.getBundleContext().getServiceReferences(GitRevisionEmitter.class, filter);
                if (!references.isEmpty()) {
                    log.info("Adding GitRevision listener {} {}" + listener.getValue(), props);
                    for (ServiceReference<GitRevisionEmitter> reference : references) {
                        GitRevisionEmitter service = context.getBundleContext().getService(reference);
                        service.addResourceListener(listener.getValue());
                        context.getBundleContext().ungetService(reference);
                    }
                } else {
                    log.info("Starting GitRevisionEmitter for listener {} {}", listener.getValue(), props);
                    Dictionary<String, String> properties = new Hashtable<>();
                    properties.put("type", (String) props.get("type"));
                    properties.put("repository", (String) props.get("repository"));
                    String branch = (String) props.get("branch");
                    properties.put("branch", branch == null ? "master" : branch);
                    AbstractGitMonitor monitor = instantiators.get(emitters.get(props.get("type")))
                                                              .apply(environment, properties);
                    monitor.addResourceListener(listener.getValue());
                    context.getBundleContext().registerService(GitRevisionEmitter.class, monitor, properties);
                }
            }
        }
        forEachService(context, (emitter) -> {
            if (emitter instanceof AbstractGitMonitor) {
                ((AbstractGitMonitor) emitter).resume();
            }
        });
        log.info("Activation complete");
    }

    @Deactivate
    protected void deactivate(ComponentContext context) {
        for (Map.Entry<Map<String, Object>, ResourceListener<GitRevision>> listener : listeners) {
            forEachService(context, emitter -> emitter.removeResourceListener(listener.getValue()));
        }
    }

    private String getEmitterFilter(Map<String, Object> props) {
        return "(&(type=" + props.get("type") + ")(repository=" + props.get("repository") + "))";
    }


    private static Map<String, Class<? extends AbstractGitMonitor>> emitters = new HashMap<>();
    private static Map<Class<? extends AbstractGitMonitor>,
            BiFunction<Environment, Dictionary<String, ?>, ? extends AbstractGitMonitor>> instantiators =
            new HashMap<>();

    static {
        emitters.put("latest", LatestRevisionGitBranchMonitor.class);
        instantiators.put(LatestRevisionGitBranchMonitor.class, LatestRevisionGitBranchMonitor::new);
    }



}
