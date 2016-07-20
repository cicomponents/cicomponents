/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.cicomponents.core;

import lombok.SneakyThrows;

import lombok.extern.slf4j.Slf4j;
import org.cicomponents.PersistentMapImplementation;
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

    private final File file;

    @SneakyThrows
    public SimpleFilePersistentMap() {
        file = new File(Paths.get("").toAbsolutePath().toString() + "/data/simplefile");
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
    @Override public Serializable get(Object key) {
        File file = new File(this.file + "/" + key);
        if (!file.exists()) {
            return null;
        }
        FileInputStream stream = new FileInputStream(file);
        return (Serializable) new ObjectInputStream(stream).readObject();
    }

    @SneakyThrows
    @Override public Serializable put(String key, Serializable value) {
                File file = new File(this.file + "/" + key);
        FileOutputStream stream = new FileOutputStream(file);
        new ObjectOutputStream(stream).writeObject(value);
        stream.close();
        return value;
    }

    @Override public Serializable remove(Object key) {
        Serializable result = get(key);
        File file = new File(this.file + "/" + key);
        file.delete();
        return result;
    }

    @Override public void putAll(Map<? extends String, ? extends Serializable> m) {
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

    @Override public Collection<Serializable> values() {
        return entrySet().stream().map(Map.Entry::getValue).collect(Collectors.toList());
    }

    @Override public Set<Entry<String, Serializable>> entrySet() {
        return Arrays.stream(file.listFiles())
                     .map(file -> new Entry<String, Serializable>() {
                         @Override public String getKey() {
                             return file.getName();
                         }

                         @Override public Serializable getValue() {
                             return get(getKey());
                         }

                         @Override public Serializable setValue(Serializable value) {
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
