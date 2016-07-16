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
import org.osgi.service.component.annotations.Component;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class PersistentMapImpl implements PersistentMap {

    private final Map<String, Object> map = new HashMap<>();

    @Override public <T extends Serializable> void put(String key, T value) {
        map.put(key, value);
    }

    @Override public <T extends Serializable> T get(String key) {
        @SuppressWarnings("unchecked")
        T value = (T) map.get(key);
        return value;
    }
}
