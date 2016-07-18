/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.cicomponents.core;

import lombok.extern.slf4j.Slf4j;
import org.cicomponents.PersistentMap;
import org.cicomponents.PersistentMapImplementation;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component(scope = ServiceScope.SINGLETON)
public class PersistentMapImpl implements PersistentMap {

    @Reference
    protected volatile PersistentMapImplementation map;

    @Override public <T extends Serializable> void put(String key, T value) {
        map.put(key, value);
    }

    @Override public <T extends Serializable> T get(String key) {
        return map.get(key);
    }
}
