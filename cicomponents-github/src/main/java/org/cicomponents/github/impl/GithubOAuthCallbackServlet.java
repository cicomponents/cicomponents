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
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.servlet.Servlet;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

@WebServlet(
        asyncSupported=true,
        urlPatterns={"/oauth/github/callback"}
)
@Slf4j
@Component
public class GithubOAuthCallbackServlet extends HttpServlet implements Servlet {

    @Reference
    protected volatile GithubOAuthFinalizer finalizer;

    @SneakyThrows
    @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        String error = req.getParameter("error");

        if (error != null && error != "") {
            resp.sendError(500, req.getParameter("error_description"));
            return;
        }
        String code = req.getParameter("code");
        String state = req.getParameter("state");
        finalizer.finalizeOAuth(UUID.fromString(state), code);
        resp.setStatus(200);
        resp.setContentType("text/html");
        resp.getWriter().write("<html><body>You have <b>successfully authorized</b> CI Components." +
                                       "You can close this page now." +
                                       "<script>window.close();</script></body></html>");
    }
}
