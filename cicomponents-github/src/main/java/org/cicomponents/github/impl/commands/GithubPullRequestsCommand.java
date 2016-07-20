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
import org.cicomponents.git.GitRevisionEmitter;
import org.cicomponents.github.GithubPullRequestEmitter;
import org.cicomponents.github.impl.PullRequestMonitor;
import org.kohsuke.github.GHPullRequest;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

import java.util.Collection;
import java.util.Map;

@Command(scope = "github", name = "pr", description = "List open GitHub Pull Requests")
@Service
public class GithubPullRequestsCommand implements Action {
    @Override public Object execute() throws Exception {
        BundleContext context = FrameworkUtil.getBundle(GithubPullRequestsCommand.class).getBundleContext();
        Collection<ServiceReference<GithubPullRequestEmitter>> references = context
                .getServiceReferences(GithubPullRequestEmitter.class, "(github-repository=*)");

        // Build the table
        ShellTable table = new ShellTable();

        table.column("Repository").alignLeft();
        table.column("PR").alignRight();
        table.column("Title").alignLeft();
        table.emptyTableText("No open GitHub PRs found");

        for (ServiceReference<GithubPullRequestEmitter> reference : references) {
            GithubPullRequestEmitter service = context.getService(reference);
            if (service instanceof PullRequestMonitor) {
                String repository = (String) reference.getProperty("github-repository");
                for (Map.Entry<Integer, GHPullRequest> entry :
                     ((PullRequestMonitor) service).getOpenPullRequests().entrySet()) {
                    GHPullRequest pullRequest = entry.getValue();
                    table.addRow().addContent(repository, "#" + pullRequest.getNumber(),
                                              pullRequest.getTitle());
                }
            }
            context.ungetService(reference);
        }

        // Print it
        table.print(System.out);
        return null;
    }
}
