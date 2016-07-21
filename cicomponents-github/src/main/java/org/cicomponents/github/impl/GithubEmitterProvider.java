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
import org.cicomponents.ResourceListener;
import org.cicomponents.github.GithubPullRequest;
import org.cicomponents.github.GithubPullRequestEmitter;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.*;

import java.util.Collection;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;
import java.util.function.Consumer;

@Component(immediate = true, scope = ServiceScope.SINGLETON)
@Slf4j
public class GithubEmitterProvider {

    @Reference
    protected Environment environment;

    @Reference(policyOption = ReferencePolicyOption.GREEDY)
    protected Collection<Map.Entry<Map<String, Object>, ResourceListener<GithubPullRequest>>> listeners;

    @SneakyThrows
    private void forEachService(ComponentContext context, Consumer<GithubPullRequestEmitter> consumer) {
        Collection<ServiceReference<GithubPullRequestEmitter>> references =
                context.getBundleContext().getServiceReferences(GithubPullRequestEmitter.class,
                                                                "(objectClass=" + GithubPullRequestEmitter.class.getName() + ")");
        for (ServiceReference<GithubPullRequestEmitter> reference : references) {
            GithubPullRequestEmitter service = context.getBundleContext().getService(reference);
            consumer.accept(service);
            context.getBundleContext().ungetService(reference);
        }
    }


    @SneakyThrows
    @Activate
    protected void activate(ComponentContext context) {
        log.info("Activating with {} potential listener(s)", listeners.size());
        forEachService(context, (emitter) -> {
            if (emitter instanceof AbstractGithubPullRequestEmitter) {
                ((AbstractGithubPullRequestEmitter) emitter).pause();
            }
        });
        for (Map.Entry<Map<String, Object>, ResourceListener<GithubPullRequest>> listener : listeners) {
            if (listener.getValue().isMatchingListener(GithubPullRequest.class)) {
                Map<String, Object> props = listener.getKey();
                String filter = getEmitterFilter(props);
                Collection<ServiceReference<GithubPullRequestEmitter>> references =
                        context.getBundleContext().getServiceReferences(GithubPullRequestEmitter.class, filter);

                if (!references.isEmpty()) {
                    log.info("Adding GithubPullRequest listener {} {}" + listener.getValue(), props);
                    for (ServiceReference<GithubPullRequestEmitter> reference : references) {
                        GithubPullRequestEmitter service = context.getBundleContext().getService(reference);
                        service.addResourceListener(listener.getValue());
                        context.getBundleContext().ungetService(reference);
                    }
                } else {
                    log.info("Starting GithubPullRequest for listener {} {}", listener.getValue(), props);
                    Dictionary<String, String> properties = new Hashtable<>();
                    String repository = getRepository(props);
                    properties.put("repository", repository);
                    properties.put("github-repository", normalizeRepository(repository));
                    AbstractGithubPullRequestEmitter emitter = new PullRequestMonitor(environment, properties);
                    emitter.addResourceListener(listener.getValue());
                    context.getBundleContext().registerService(GithubPullRequestEmitter.class, emitter, properties);
                }
            }
        }
        forEachService(context, (emitter) -> {
            if (emitter instanceof AbstractGithubPullRequestEmitter) {
                ((AbstractGithubPullRequestEmitter) emitter).resume();
            }
        });

        log.info("Activation complete");
    }

    @Deactivate
    protected void deactivate(ComponentContext context) {
        for (Map.Entry<Map<String, Object>, ResourceListener<GithubPullRequest>> listener : listeners) {
            forEachService(context, emitter -> emitter.removeResourceListener(listener.getValue()));
        }
    }

    private String getEmitterFilter(Map<String, Object> props) {
        return "(repository=" + getRepository(props) + ")";
    }


    private String getRepository(Map<String, Object> props) {
        if (props.containsKey("repository")) {
            return (String) props.get("repository");
        }
        if (props.containsKey("github-repository")) {
            return "https://github.com/" + props.get("github-repository");
        }
        throw new IllegalArgumentException("No repository provided in " + props);
    }

    private String normalizeRepository(String repository) {
        return repository
                .replaceAll("^https://github\\.com/", "")
                .replaceAll("^git://github\\.com/", "")
                .replaceAll("^github\\.com/", "")
                .replaceAll("\\.git$","");
    }


}
