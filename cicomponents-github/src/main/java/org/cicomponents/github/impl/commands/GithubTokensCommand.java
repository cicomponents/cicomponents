/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.cicomponents.github.impl.commands;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.apache.karaf.shell.table.ShellTable;
import org.cicomponents.github.GithubOAuthTokenProvider;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

import java.util.Collection;

@Command(scope = "github", name = "tokens", description = "List available GitHub tokens")
@Service
public class GithubTokensCommand implements Action {
    @Override public Object execute() throws Exception {
        BundleContext context = FrameworkUtil.getBundle(GithubTokensCommand.class).getBundleContext();
        Collection<ServiceReference<GithubOAuthTokenProvider>> references = context
                .getServiceReferences(GithubOAuthTokenProvider.class, "(objectClass=" + GithubOAuthTokenProvider
                        .class.getName() + ")");

        // Build the table
        ShellTable table = new ShellTable();

        table.column("Repository").alignLeft();
        table.column("Token").alignLeft();
        table.emptyTableText("No tokens available");

        for (ServiceReference<GithubOAuthTokenProvider> reference : references) {
            GithubOAuthTokenProvider service = context.getService(reference);
            table.addRow().addContent(service.getRepository(), service.getAccessToken());
            context.ungetService(reference);
        }

        // Print it
        table.print(System.out);

        return null;
    }
}
