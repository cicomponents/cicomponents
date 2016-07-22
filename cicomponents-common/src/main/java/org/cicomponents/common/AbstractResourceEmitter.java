/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.cicomponents.common;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.cicomponents.Resource;
import org.cicomponents.ResourceEmitter;
import org.cicomponents.ResourceHolder;
import org.cicomponents.ResourceListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Slf4j
public class AbstractResourceEmitter<T extends Resource> implements ResourceEmitter<T> {
    @Getter
    private Collection<ResourceListener<T>> listeners = new ArrayList<>();

    private static final ExecutorService executor = Executors.newCachedThreadPool(
            new ThreadFactoryBuilder().setNameFormat("resource-emitter-%d").build()
    );

    @Override public void addResourceListener(ResourceListener<T> listener) {
        listeners.add(listener);
    }

    @Override public void removeResourceListener(ResourceListener<T> listener) {
        listeners.remove(listener);
    }

    protected void emit(ResourceHolder<T> holder) {
        T resource = holder.acquire();

        List<Callable<Void>> callables = listeners.stream().map(listener -> (Callable<Void>) () -> {
            listener.onEmittedResource(holder, AbstractResourceEmitter.this);
            return null;
        }).collect(Collectors.toList());

        try {
            executor.invokeAll(callables);
        } catch (InterruptedException e) {
        } finally {
            resource.close();
        }
    }
}
