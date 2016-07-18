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
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;

import java.io.*;
import java.nio.file.Paths;

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

    @SneakyThrows
    @Override public <T extends Serializable> void put(String key, T value) {
        File file = new File(this.file + "/" + key);
        FileOutputStream stream = new FileOutputStream(file);
        new ObjectOutputStream(stream).writeObject(value);
        stream.close();

    }

    @SneakyThrows
    @Override public <T extends Serializable> T get(String key) {
        File file = new File(this.file + "/" + key);
        if (!file.exists()) {
            return null;
        }
        FileInputStream stream = new FileInputStream(file);
        @SuppressWarnings("unchecked")
        T t = (T) new ObjectInputStream(stream).readObject();
        return t;
    }
}
