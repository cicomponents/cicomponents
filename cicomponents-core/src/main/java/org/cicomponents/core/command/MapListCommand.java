/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.cicomponents.core.command;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.apache.karaf.shell.table.ShellTable;
import org.cicomponents.PersistentMap;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

import java.io.Serializable;
import java.util.Map;

@Command(scope = "map", name = "list", description = "List keys and values in PersistentMap")
@Service
public class MapListCommand implements Action {
    @Override public Object execute() throws Exception {
        BundleContext context = FrameworkUtil.getBundle(MapListCommand.class).getBundleContext();
        ServiceReference<PersistentMap> reference = context
                .getServiceReference(PersistentMap.class);
        PersistentMap map = context.getService(reference);

        // Build the table
        ShellTable table = new ShellTable();

        table.column("Key").alignLeft();
        table.column("Value").alignLeft();
        table.emptyTableText("No keys defined");

        for (Map.Entry<String, Serializable> entry : map.entrySet()) {
            table.addRow().addContent(entry.getKey(), entry.getValue());
        }

        // Print it
        table.print(System.out);

        context.ungetService(reference);

        return null;
    }
}
