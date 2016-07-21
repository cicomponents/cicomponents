/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.cicomponents.core;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.cicomponents.PersistentMapImplementation;
import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;
import org.osgi.framework.Bundle;
import org.osgi.framework.wiring.BundleWiring;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.ServiceScope;

import java.io.ByteArrayOutputStream;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component(property = "type=simplefile", scope = ServiceScope.SINGLETON)
public class MVStorePersistentMapImplementation implements PersistentMapImplementation {

    private MVStore store;

    @Activate
    protected void activate() {
        store = MVStore.open(Paths.get("").toAbsolutePath().toString() + "/data/pmap.mvstore");
    }

    @Deactivate
    protected void deactivate() {
        store.close();
    }

    @Override public <T> Map<String, T> getMapForBundle(Bundle bundle) {
        return new Backend<>(store, bundle);
    }

    private final class Backend<T> implements Map<String, T> {
        private final Kryo kryo;
        private final MVMap<String, byte[]> map;

        @SneakyThrows
        public Backend(MVStore store, Bundle bundle) {
            kryo = new Kryo();
            kryo.setClassLoader(bundle.adapt(BundleWiring.class).getClassLoader());
            map = store.openMap(bundle.getSymbolicName());
        }

        private byte[] serialize(T value) {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            Output output = new Output(bos);
            kryo.writeClassAndObject(output, value);
            output.close();
            return bos.toByteArray();
        }

        private T deserialize(byte[] bytes) {
            if (bytes == null) {
                return null;
            }
            @SuppressWarnings("unchecked")
            T result = (T) kryo.readClassAndObject(new Input(bytes));
            return result;
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
            @SuppressWarnings("unchecked")
            boolean b = map.containsValue(serialize((T) value));
            return b;
        }

        @SneakyThrows
        @Override public T get(Object key) {
            return deserialize(map.get(key));
        }

        @SneakyThrows
        @Override public T put(String key, T value) {
            return deserialize(map.put(key, serialize(value)));
        }

        @Override public T remove(Object key) {
            return deserialize(map.remove(key));
        }

        @Override public void putAll(Map<? extends String, ? extends T> m) {
            map.putAll(m.entrySet().stream()
                        .collect(Collectors.toMap(Entry::getKey, e -> serialize(e.getValue()))));
        }

        @SneakyThrows
        @Override public void clear() {
            map.clear();
        }

        @Override public Set<String> keySet() {
            return map.keySet();
        }

        @Override public Collection<T> values() {
            return map.values().stream().map(this::deserialize).collect(Collectors.toList());
        }

        @Override public Set<Entry<String, T>> entrySet() {
            return map.entrySet().stream().map(PMapEntry::new).collect(Collectors.toSet());
        }

        private class PMapEntry implements Entry<String, T> {
            private final Entry<String, byte[]> entry;

            public PMapEntry(Entry<String, byte[]> entry) {this.entry = entry;}

            @Override public String getKey() {
                return entry.getKey();
            }

            @Override public T getValue() {
                return deserialize(entry.getValue());
            }

            @Override public T setValue(T value) {
                return deserialize(entry.setValue(serialize(value)));
            }

            @Override public boolean equals(Object o) {
                return o instanceof Map.Entry &&
                        entry.getKey() == ((Entry) o).getKey() &&
                        entry.getValue() == ((Entry) o).getValue();
            }

            @Override public int hashCode() {
                return entry.hashCode();
            }
        }
    }

}
