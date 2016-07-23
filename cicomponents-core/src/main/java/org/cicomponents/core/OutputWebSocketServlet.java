/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.cicomponents.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.extern.slf4j.Slf4j;
import org.cicomponents.OutputProvider;
import org.cicomponents.OutputProviderRegistry;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;
import org.eclipse.jetty.websocket.servlet.*;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.servlet.Servlet;
import javax.servlet.annotation.WebServlet;
import java.io.IOException;
import java.util.Iterator;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@WebServlet(
        asyncSupported=true,
        urlPatterns="/output/ws/*"
)
@Slf4j
@Component
public class OutputWebSocketServlet extends WebSocketServlet implements Servlet {
    @Reference
    protected OutputProviderRegistry registry;

    @Override public void configure(WebSocketServletFactory factory) {
        factory.setCreator((req, resp) -> new Socket(registry));
    }

    static ExecutorService executor = Executors.newCachedThreadPool(
            new ThreadFactoryBuilder().setNameFormat("output-ws-%d").build()
    );

    private static class Socket extends WebSocketAdapter {
        private final OutputProviderRegistry registry;

        public Socket(OutputProviderRegistry registry) {
            this.registry = registry;
        }

        @Override public void onWebSocketConnect(Session sess) {
            String[] pathComponents = sess.getUpgradeRequest().getRequestURI().getPath().split("/");
            UUID uuid = UUID.fromString(pathComponents[pathComponents.length - 1]);
            OutputProvider provider = registry.getProviders().get(uuid);
            ObjectMapper mapper = new ObjectMapper();
            executor.execute(() -> {
                Iterator<OutputProvider.TimestampedOutput> iterator = provider.getOutput().iterator();
                while (iterator.hasNext()) {
                    if (!sess.isOpen()) {
                        return;
                    }
                    try {
                        sess.getRemote().sendString(mapper.writeValueAsString(iterator.next()));
                    } catch (Exception e) {
                        log.error("Exception while serializing a timestamped output", e);
                        try {
                            sess.disconnect();
                        } catch (IOException e1) {
                        }
                    }
                }
            });
        }
    }
}
