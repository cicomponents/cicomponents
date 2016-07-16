/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.cicomponents.git.impl;

import lombok.Getter;
import org.cicomponents.PersistentMap;
import org.cicomponents.fs.WorkingDirectoryProvider;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component
public class EnvironmentImpl implements Environment {
    @Getter
    @Reference
    protected volatile WorkingDirectoryProvider workingDirectoryProvider;
    @Getter
    @Reference
    protected volatile PersistentMap persistentMap;

}
