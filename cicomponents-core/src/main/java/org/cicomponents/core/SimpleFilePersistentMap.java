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
import org.osgi.framework.Bundle;
import org.osgi.framework.wiring.BundleWiring;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component(property = "type=simplefile", scope = ServiceScope.SINGLETON)
public class SimpleFilePersistentMap implements PersistentMapImplementation {

    @Override public <T> Map<String, T> getMapForBundle(Bundle bundle) {
        return new Backend<>(bundle);
    }

    private final class Backend<T> implements Map<String, T> {
        private final File file;
        private final Kryo kryo;

        @SneakyThrows
        public Backend(Bundle bundle) {
            kryo = new Kryo();
            kryo.setClassLoader(bundle.adapt(BundleWiring.class).getClassLoader());
            file = new File(Paths.get("").toAbsolutePath().toString() + "/data/simplefile/" + bundle.getBundleId());
            boolean mkdirs = file.mkdirs();
            log.info("Creating a persistent map at {}, created: {}", file, mkdirs);
            assert mkdirs;
        }

        @Override public int size() {
            return file.listFiles().length;
        }

        @Override public boolean isEmpty() {
            return size() == 0;
        }

        @Override public boolean containsKey(Object key) {
            File file = new File(this.file + "/" + key);
            return file.exists();
        }

        @Override public boolean containsValue(Object value) {
            return values().contains(value);
        }

        @SneakyThrows
        @Override public T get(Object key) {
            File file = new File(this.file + "/" + key);
            if (!file.exists()) {
                return null;
            }
            FileInputStream stream = new FileInputStream(file);
            return (T) kryo.readClassAndObject(new Input(stream));
        }

        @SneakyThrows
        @Override public T put(String key, T value) {
            File file = new File(this.file + "/" + key);
            FileOutputStream stream = new FileOutputStream(file);
            Output output = new Output(stream);
            kryo.writeClassAndObject(output, value);
            output.close();
            return value;
        }

        @Override public T remove(Object key) {
            T result = get(key);
            File file = new File(this.file + "/" + key);
            file.delete();
            return result;
        }

        @Override public void putAll(Map<? extends String, ? extends T> m) {
            m.forEach(this::put);
        }

        @SneakyThrows
        @Override public void clear() {
            Files.walkFileTree(file.toPath(), new SimpleFileVisitor<Path>() {
                @Override public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                        throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
            file.mkdirs();
        }

        @Override public Set<String> keySet() {
            return Arrays.stream(file.listFiles()).map(File::getName).collect(Collectors.toSet());
        }

        @Override public Collection<T> values() {
            return entrySet().stream().map(Map.Entry::getValue).collect(Collectors.toList());
        }

        @Override public Set<Entry<String, T>> entrySet() {
            return Arrays.stream(file.listFiles())
                         .map(file -> new Entry<String, T>() {
                             @Override public String getKey() {
                                 return file.getName();
                             }

                             @Override public T getValue() {
                                 return get(getKey());
                             }

                             @Override public T setValue(T value) {
                                 return put(getKey(), value);
                             }

                             @Override public boolean equals(Object o) {
                                 return get(getKey()).equals(o);
                             }

                             @Override public int hashCode() {
                                 return getKey().hashCode();
                             }
                         }).collect(Collectors.toSet());
        }
    }

}
