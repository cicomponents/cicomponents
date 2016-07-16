/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.cicomponents.common;

import lombok.Getter;
import lombok.Synchronized;
import org.cicomponents.Resource;
import org.cicomponents.ResourceEmitter;
import org.cicomponents.ResourceHolder;
import org.cicomponents.ResourceListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AbstractResourceEmitter<T extends Resource> implements ResourceEmitter<T> {
    @Getter
    private Collection<ResourceListener<T>> listeners = new ArrayList<>();

    private boolean shouldLog = true;
    private Collection<ResourceHolder<T>> log = new ArrayList<>();
    private Collection<T> acquiredResources = new ArrayList<>();

    public AbstractResourceEmitter() {
        // Since we don't know upfront the finite set of subscribers, we give them
        // some time to subscribe (30 seconds) and then stop logging altogether.
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        executor.schedule(this::stopLogging, 30, TimeUnit.SECONDS);
    }

    @Synchronized("log")
    protected void stopLogging() {
        shouldLog = false;
        log.clear();
        acquiredResources.forEach(Resource::close);
    }

    @Synchronized("log")
    @Override public void addResourceListener(ResourceListener<T> listener) {
        listeners.add(listener);
        for (ResourceHolder<T> holder : log) {
            listener.onEmittedResource(holder, this);
        }
    }

    @Synchronized("log")
    @Override public void removeResourceListener(ResourceListener<T> listener) {
        listeners.remove(listener);
    }

    @Synchronized("log")
    protected void emit(ResourceHolder<T> holder) {
        T resource = holder.acquire();
        acquiredResources.add(resource);
        listeners.forEach(listener -> listener.onEmittedResource(holder, this));
        if (shouldLog) {
            log.add(holder);
        } else {
            resource.close();
        }
    }

}
