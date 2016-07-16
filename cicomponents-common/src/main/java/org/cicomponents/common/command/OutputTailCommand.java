/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.cicomponents.common.command;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.cicomponents.OutputProvider;
import org.cicomponents.common.OutputProviderRegistry;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

import java.util.UUID;

import static org.cicomponents.OutputProvider.TimestampedOutput.Kind.STDERR;

@Command(scope = "output", name = "tail", description = "Watch an output")
@Service
public class OutputTailCommand implements Action {
    @Argument(index = 0, name = "id", description = "Output ID", required = true, multiValued = false)
    protected String uuid;

    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_RESET = "\u001B[0m";


    @Override public Object execute() throws Exception {
        BundleContext context = FrameworkUtil.getBundle(OutputListCommand.class).getBundleContext();
        ServiceReference<OutputProviderRegistry> reference = context
                .getServiceReference(OutputProviderRegistry.class);
        OutputProviderRegistry registry = context.getService(reference);

        OutputProvider provider = registry.getProviders().get(UUID.fromString(uuid));

        try {
            provider.getOutput().forEachOrdered(timestampedOutput -> {
                if (timestampedOutput.getKind() == STDERR) {
                    System.out.print(ANSI_RED);
                }
                System.out.print(new String(timestampedOutput.getOutput()));
                if (timestampedOutput.getKind() == STDERR) {
                    System.out.print(ANSI_RESET);
                }

            });
        } finally {
            System.out.println(); // newline
            context.ungetService(reference);
        }
        return null;
    }
}
