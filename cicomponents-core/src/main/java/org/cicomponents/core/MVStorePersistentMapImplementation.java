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
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
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

    @Override public <T> Map<String, T> getMapForBundle(Bundle bundle, CharSequence namespace) {
        return new Backend<>(store, bundle, namespace);
    }

    private final class Backend<T> implements Map<String, T> {
        private final Kryo kryo;
        private final MVMap<String, byte[]> map;

        @SneakyThrows
        public Backend(MVStore store, Bundle bundle, CharSequence namespace) {
            kryo = new Kryo();
            kryo.setClassLoader(bundle.adapt(BundleWiring.class).getClassLoader());
            map = store.openMap(namespace.toString());
        }

        @SneakyThrows
        private byte[] serialize(T value) {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            Output output = new Output(bos);
            kryo.writeClassAndObject(output, value);
            output.close();
            return bos.toByteArray();
        }

        @SneakyThrows
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
            return values().contains(value);
        }

        @Override public T get(Object key) {
            return deserialize(map.get(key));
        }

        @Override public T put(String key, T value) {
            if (key == null) {
                throw new RuntimeException("null keys not allowed");
            }
            return deserialize(map.put(key, serialize(value)));
        }

        @Override public T remove(Object key) {
            return deserialize(map.remove(key));
        }

        @Override public void putAll(Map<? extends String, ? extends T> m) {
            map.putAll(m.entrySet().stream()
                        .collect(Collectors.toMap(Entry::getKey, e -> serialize(e.getValue()))));
        }

        @Override public void clear() {
            map.clear();
        }

        @Override public Set<String> keySet() {
            return map.keySet();
        }

        @Override public Collection<T> values() {
            return
            new AbstractCollection<T>() {
                public Iterator<T> iterator() {
                    return new Iterator<T>() {
                        private Iterator<Entry<String,byte[]>> i = map.entrySet().iterator();

                        public boolean hasNext() {
                            return i.hasNext();
                        }

                        public T next() {
                            return deserialize(i.next().getValue());
                        }

                        public void remove() {
                            i.remove();
                        }
                    };
                }

                public int size() {
                    return map.size();
                }

                public boolean isEmpty() {
                    return map.isEmpty();
                }

                public void clear() {
                    map.clear();
                }

                public boolean contains(Object v) {
                    Iterator<T> iterator = iterator();
                    while (iterator.hasNext()) {
                        T next = iterator.next();
                        if (next == null) {
                            if (v == null) {
                                return true;
                            }
                        } else {
                            if (next.equals(v)) {
                                return true;
                            }
                        }
                    }
                    return false;
                }
            };
        }

        @Override public Set<Entry<String, T>> entrySet() {
            return map.entrySet().stream().map(PMapEntry::new).collect(Collectors.toSet());
        }

        @Override public int hashCode() {
            int h = 0;
            Iterator<Entry<String, T>> i = entrySet().iterator();
            while (i.hasNext())
                h += i.next().hashCode();
            return h;
        }

        @Override public boolean equals(Object obj) {
            return obj instanceof Map &&
                   entrySet().equals(((Map) obj).entrySet());
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
                T value = getValue();
                return o instanceof Entry &&
                        getKey().equals(((Entry) o).getKey()) &&
                        ((value == null && ((Entry) o).getValue() == null) ||
                         (value != null && value.equals(((Entry) o).getValue())));
            }

            @Override public int hashCode() {
                T value = getValue();
                return getKey().hashCode() ^ (value == null ? 0 : value.hashCode());
            }

        }
    }

}
