/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.cicomponents.core.command;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.apache.karaf.shell.table.ShellTable;
import org.cicomponents.PersistentMap;
import org.cicomponents.core.PersistentMapImpl;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

import java.util.Map;

@Command(scope = "pmap", name = "list", description = "List keys and values in PersistentMap")
@Service
public class MapListCommand implements Action {
    @Argument(description = "Bundle ID")
    protected String bundleId;

    @Override public Object execute() throws Exception {
        Bundle bundle = FrameworkUtil.getBundle(MapListCommand.class);
        BundleContext context = bundle.getBundleContext();
        ServiceReference<PersistentMap> reference = context
                .getServiceReference(PersistentMap.class);

        if (bundleId != null) {
            Bundle targetBundle = bundle.getBundleContext().getBundle(Long.valueOf(bundleId));
            ShellTable shellTable = printKeys(context, reference, targetBundle, false);
            shellTable.print(System.out);
        } else {
            for (Bundle targetBundle : bundle.getBundleContext().getBundles()) {
                ShellTable shellTable = printKeys(context, reference, targetBundle, true);
                if (shellTable != null) {
                    System.out.println();
                    System.out.println(targetBundle.getBundleId() + " - " + targetBundle.getSymbolicName());
                    System.out.println();
                    shellTable.print(System.out);
                }
            }
        }

        return null;
    }

    private ShellTable printKeys(BundleContext context, ServiceReference<PersistentMap> reference,
                           Bundle targetBundle, boolean nullIfEmpty) {
        Map<String, Object> map = ((PersistentMapImpl)context.getService(reference))
                                   .getImplementation().getMapForBundle(targetBundle);

        // Build the table
        ShellTable table = new ShellTable();

        table.column("Key").alignLeft();
        table.column("Value").alignLeft();
        table.emptyTableText("No keys defined");

        for (Map.Entry<String, ?> entry : map.entrySet()) {
            table.addRow().addContent(entry.getKey(), entry.getValue());
        }

        if (map.isEmpty() && nullIfEmpty) {
            table = null;
        }

        context.ungetService(reference);

        return table;
    }
}
