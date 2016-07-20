/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.cicomponents.github.impl;

import com.github.scribejava.apis.GitHubApi;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.google.common.collect.Iterables;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import org.cicomponents.PersistentMap;
import org.cicomponents.github.GithubApplicationCredentialsProvider;
import org.cicomponents.github.GithubOAuthTokenProvider;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.*;
import org.osgi.service.component.annotations.Component;

import java.awt.Desktop;
import java.net.URI;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@Component(immediate = true, scope = ServiceScope.SINGLETON)
public class GithubOAuthTokenProvisioner implements GithubOAuthFinalizer {

    @Reference
    protected PersistentMap persistentMap;

    private ComponentContext context;

    @Activate
    protected void activate(ComponentContext context) {
        this.context = context;
    }

    private Map<UUID, Collection<OAuthTokenProvider>> registrations = new HashMap<>();

    @SneakyThrows
    @Override public void finalizeOAuth(UUID uuid, String code) {
        Collection<OAuthTokenProvider> providers = registrations.get(uuid);
        providers.forEach(new Consumer<OAuthTokenProvider>() {
            @SneakyThrows
            @Override public void accept(OAuthTokenProvider p) {
                p.setAccessToken(p.getService().getAccessToken(code));
            }
        });
        ArrayList<String> tokens = new ArrayList<>(
                providers.stream().map(OAuthTokenProvider::getAccessToken).collect(Collectors.toList()));
        persistentMap.put(Iterables.getLast(providers).getCredentialsProvider().getClientId(), tokens);
        Collection<ServiceRegistration<GithubOAuthTokenProvider>> registrations = registerProviders(providers);
        this.registrations.remove(uuid);
        tokenProviders.addAll(registrations);
        providers.forEach(p -> pendingProvisioning.remove(p.getCredentialsProvider().getClientId()));
    }

    private Collection<ServiceRegistration<GithubOAuthTokenProvider>> registerProviders(
            Collection<OAuthTokenProvider> providers) {
        return providers.stream()
                 .map(p -> {
                     Hashtable<String, Object> props = new Hashtable<>();
                     props.put("github-repository", p.getRepository());
                     ServiceRegistration<GithubOAuthTokenProvider> registration =
                             context.getBundleContext()
                                    .registerService(GithubOAuthTokenProvider.class, p, props);
                     return registration;
                 })
                 .collect(Collectors.toList());
    }

    private Collection<ServiceRegistration<GithubOAuthTokenProvider>> tokenProviders = new ArrayList<>();

    private Set<String> pendingProvisioning = new HashSet<>();

    private static class OAuthTokenProvider implements GithubOAuthTokenProvider {
        @Getter
        private final GithubApplicationCredentialsProvider credentialsProvider;
        @Getter
        private OAuth20Service service;
        @Getter
        private final String repository;

        @Getter @Setter
        private OAuth2AccessToken accessToken;

        private OAuthTokenProvider(GithubApplicationCredentialsProvider credentialsProvider,
                                   OAuth20Service oAuth20Service,
                                   String repository) {
            this.credentialsProvider = credentialsProvider;
            service = oAuth20Service;
            this.repository = repository;
        }

        private OAuthTokenProvider(GithubApplicationCredentialsProvider credentialsProvider,
                                   OAuth2AccessToken accessToken,
                                   String repository) {
            this.credentialsProvider = credentialsProvider;
            this.accessToken = accessToken;
            this.repository = repository;
        }

        @SneakyThrows
        public String getAccessToken() {
            return accessToken.getAccessToken();
        }
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    @SneakyThrows
    @Synchronized("registrations")
    protected void addApplicationCredentialsProvider(GithubApplicationCredentialsProvider provider) {
        ArrayList<OAuth2AccessToken> tokens = (ArrayList<OAuth2AccessToken>) persistentMap.get(provider.getClientId());
        if (tokens == null) {
            if (pendingProvisioning.contains(provider.getClientId())) {
                return;
            }
            pendingProvisioning.add(provider.getClientId());
            UUID uuid = UUID.randomUUID();
            OAuth20Service service = new ServiceBuilder()
                    .apiKey(provider.getClientId())
                    .apiSecret(provider.getClientSecret())
                    .scope("write:repo_hook, read:repo_hook, repo")
                    .state(uuid.toString())
                    .callback("") // use GitHub's configured value
                    .build(GitHubApi.instance());

            String authorizationUrl = service.getAuthorizationUrl();

            log.info("Open the following URL to authorize GitHub repo access: {}", authorizationUrl);
            System.out.println("Open the following URL to authorize GitHub access: " + authorizationUrl);
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(URI.create(authorizationUrl));
            }

            List<OAuthTokenProvider> providers = provider.getRepositories()
                                                         .stream().map(r -> new OAuthTokenProvider(provider, service, r))
                                                         .collect(Collectors.toList());

            registrations.put(uuid, providers);
        } else {
            List<OAuthTokenProvider> providers = IntStream.range(0, provider.getRepositories().size())
                                                        .mapToObj(i ->
                                                                          new OAuthTokenProvider(provider,
                                                                                                 tokens.get(i),
                                                                                                 provider.getRepositories()
                                                                                                         .get(i)))
                                                        .collect(Collectors.toList());
            tokenProviders.addAll(registerProviders(providers));
        }
    }

    @Synchronized("registrations")
    protected void removeApplicationCredentialsProvider(GithubApplicationCredentialsProvider provider) {
    }
}
