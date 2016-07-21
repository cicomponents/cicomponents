/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.cicomponents.github.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.cicomponents.github.GithubPullRequestEmitter;
import org.kohsuke.github.GHEventPayload;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.servlet.AsyncContext;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ForkJoinPool;

@WebServlet(
        asyncSupported=true,
        urlPatterns={GithubPRWebhookServlet.PATH}
)
@Slf4j
@Component
public class GithubPRWebhookServlet extends HttpServlet implements Servlet {
    static final String PATH = "/github/webhook/pr";

    @Reference
    protected volatile List<GithubPullRequestEmitter> emitters = new ArrayList<>();

    @Override protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        AsyncContext asyncContext = req.startAsync();
        ForkJoinPool.commonPool().execute(new Runnable() {
            @SneakyThrows
            @Override public void run() {
                Map<String, Object> payload = new ObjectMapper().readValue(req.getReader(),
                                                                           new TypeReference<Map<String, Object>>() {});

                String action = (String) payload.get("action");
                Integer number = (Integer) payload.get("number");
                @SuppressWarnings("unchecked")
                String name = (String) ((Map<String, Object>) payload.get("repository")).get("full_name");

                if (action.contentEquals("synchronize") ||
                        action.contentEquals("closed") ||
                        action.contentEquals("opened") ||
                        action.contentEquals("reopened")) {

                    emitters.stream()
                            .filter(emitter -> emitter.getRepository().contentEquals(name))
                            .forEach(emitter -> emitter.onPullRequestEvent(number));

                }

                asyncContext.complete();
            }
        });
    }
}
