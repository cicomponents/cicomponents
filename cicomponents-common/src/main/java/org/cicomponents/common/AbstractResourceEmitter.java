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
import lombok.extern.slf4j.Slf4j;
import org.cicomponents.Resource;
import org.cicomponents.ResourceEmitter;
import org.cicomponents.ResourceHolder;
import org.cicomponents.ResourceListener;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
public class AbstractResourceEmitter<T extends Resource> implements ResourceEmitter<T> {
    @Getter
    private Collection<ResourceListener<T>> listeners = new ArrayList<>();

    @Override public void addResourceListener(ResourceListener<T> listener) {
        listeners.add(listener);
    }

    @Override public void removeResourceListener(ResourceListener<T> listener) {
        listeners.remove(listener);
    }

    protected void emit(ResourceHolder<T> holder) {
        T resource = holder.acquire();
        listeners.forEach(listener -> listener.onEmittedResource(holder, this));
        resource.close();
    }
}
