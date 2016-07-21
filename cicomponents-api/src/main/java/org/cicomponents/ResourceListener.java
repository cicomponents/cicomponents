/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.cicomponents;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

@FunctionalInterface
public interface ResourceListener<T extends Resource> {
    void onEmittedResource(ResourceHolder<T> holder,
                           ResourceEmitter<T> emitter);

    default boolean isMatchingListener(Class klass) {
        for (Type type : getClass().getGenericInterfaces()) {
            if (type instanceof ParameterizedType &&
                    ((ParameterizedType) type).getRawType() == ResourceListener.class) {
                ParameterizedType parameterizedType = (ParameterizedType) type;
                Type target = parameterizedType.getActualTypeArguments()[0];
                if (target == klass) {
                    return true;
                }
            }
        }
        return false;
    }
}
