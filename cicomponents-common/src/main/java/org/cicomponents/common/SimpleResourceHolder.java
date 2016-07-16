/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.cicomponents.common;

import org.cicomponents.Resource;
import org.cicomponents.ResourceHolder;

public class SimpleResourceHolder<T extends Resource> implements ResourceHolder<T> {

    private final T resource;

    public SimpleResourceHolder(T resource) {this.resource = resource;}

    @Override public T acquire() {
        resource.acquire();
        return resource;
    }
}
