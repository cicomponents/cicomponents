/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.cicomponents.ci;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.cicomponents.PersistentMap;
import org.cicomponents.badges.BadgeMaker;
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
import java.util.Base64;
import java.util.concurrent.ForkJoinPool;

import static org.cicomponents.badges.BadgeMaker.*;

@WebServlet(
        asyncSupported=true,
        urlPatterns={"/ci/*"}
)
@Slf4j
@Component
public class StatusServlet extends HttpServlet implements Servlet {
    @Reference
    protected BadgeMaker badgeMaker;
    @Reference
    protected PersistentMap pmap;

    @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        AsyncContext asyncContext = req.startAsync();
        ForkJoinPool.commonPool().execute(new Runnable() {
            @SneakyThrows
            @Override public void run() {

                resp.setContentType("text/html");
                byte[] badge;
                if (pmap.get("build-status") == null) {
                    badge = badgeMaker.make(subject("build"), status("unknown"), statusColor("lightgrey"));
                } else {
                    String buildStatus = (String) pmap.get("build-status");
                    badge = badgeMaker.make(subject("build"), status(buildStatus),
                                            statusColor(buildStatus.contentEquals("passing") ? "brightgreen" : "red"));
                }
                resp.getWriter().write("Current status: " +
                                               "<img src=\"data:image/svg+xml;base64," + Base64.getEncoder()
                                                                                               .encodeToString(
                                                                                                       badge) + "\">");
                asyncContext.complete();
            }
        });
    }
}
