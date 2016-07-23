/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.cicomponents.core;

import com.google.common.io.CharStreams;
import lombok.extern.slf4j.Slf4j;
import org.osgi.service.component.annotations.Component;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

@WebServlet(
        asyncSupported=true,
        urlPatterns="/output/*"
)
@Slf4j
@Component
public class OutputServlet extends HttpServlet implements Servlet {
    @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("text/html");
        InputStream resourceAsStream = getClass().getResourceAsStream("output.html");
        String s = CharStreams.toString(new InputStreamReader(resourceAsStream));
        resp.getWriter().write(s);
    }
}
