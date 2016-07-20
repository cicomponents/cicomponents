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
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Slf4j
@Component(scope = ServiceScope.SINGLETON)
public class PersistentMapImpl implements PersistentMap {

    @Reference
    protected volatile PersistentMapImplementation map;

    @Override public int size() {
        return map.size();
    }

    @Override public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override public boolean containsKey(Object key) {
        return map.containsKey(key);
    }

    @Override public boolean containsValue(Object value) {
        return map.containsValue(value);
    }

    @Override public Serializable get(Object key) {
        return map.get(key);
    }

    @Override public Serializable put(String key, Serializable value) {
        return map.put(key, value);
    }

    @Override public Serializable remove(Object key) {
        return map.remove(key);
    }

    @Override public void putAll(Map<? extends String, ? extends Serializable> m) {
        map.putAll(m);
    }

    @Override public void clear() {
        map.clear();
    }

    @Override public Set<String> keySet() {
        return map.keySet();
    }

    @Override public Collection<Serializable> values() {
        return map.values();
    }

    @Override public Set<Entry<String, Serializable>> entrySet() {
        return map.entrySet();
    }
}
