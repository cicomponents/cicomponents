/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.cicomponents.core;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.cicomponents.PersistentMap;
import org.cicomponents.PersistentMapImplementation;
import org.osgi.framework.Bundle;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Slf4j
@Component(scope = ServiceScope.BUNDLE)
public class PersistentMapImpl implements PersistentMap {

    @Getter
    @Reference
    protected volatile PersistentMapImplementation implementation;

    private Map<String, Object> map;

    @Activate
    protected void activate(ComponentContext context) {
        map = implementation.getMapForBundle(context.getUsingBundle());
    }


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

    @Override public Object get(Object key) {
        return map.get(key);
    }

    @Override public Object put(String key, Object value) {
        return map.put(key, value);
    }

    @Override public Object remove(Object key) {
        return map.remove(key);
    }

    @Override public void putAll(Map<? extends String, ?> m) {
        map.putAll(m);
    }

    @Override public void clear() {
        map.clear();
    }

    @Override public Set<String> keySet() {
        return map.keySet();
    }

    @Override public Collection<Object> values() {
        return map.values();
    }

    @Override public Set<Entry<String, Object>> entrySet() {
        return map.entrySet();
    }

    @Override public int hashCode() {
        return map.hashCode();
    }

    @Override public boolean equals(Object obj) {
        return map.equals(obj);
    }
}
