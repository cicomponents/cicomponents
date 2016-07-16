/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.cicomponents.common.command;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.apache.karaf.shell.table.ShellTable;
import org.cicomponents.OutputProvider;
import org.cicomponents.common.OutputProviderRegistry;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

import java.util.Map;
import java.util.UUID;

@Command(scope = "output", name = "list", description = "List available outputs")
@Service
public class OutputListCommand implements Action {

    @Override public Object execute() throws Exception {

        BundleContext context = FrameworkUtil.getBundle(OutputListCommand.class).getBundleContext();
        ServiceReference<OutputProviderRegistry> reference = context
                .getServiceReference(OutputProviderRegistry.class);
        OutputProviderRegistry registry = context.getService(reference);

        // Build the table
        ShellTable table = new ShellTable();

        table.column("ID").alignLeft();
        table.column("Last updated").alignLeft();
        table.emptyTableText("No outputs available");

        for (Map.Entry<UUID, OutputProvider> entry : registry.getProviders().entrySet()) {
            table.addRow().addContent(entry.getKey().toString(), entry.getValue().getLatestDate().toString());
        }

        // Print it
        table.print(System.out);

        context.ungetService(reference);

        return null;
    }
}
