/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.cicomponents.ci;

import lombok.SneakyThrows;
import org.cicomponents.OutputProviderService;
import org.cicomponents.ResourceEmitter;
import org.cicomponents.ResourceHolder;
import org.cicomponents.ResourceListener;
import org.cicomponents.git.GitRevision;
import org.cicomponents.git.GitRevisionEmitter;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;

@Component(property = {"type=latest", "repository=https://github.com/cicomponents/cicomponents", "branch=master"})
public class MasterListener implements ResourceListener<GitRevision> {

    /**
     * This service is used to create outputs
     */
    @Reference
    protected OutputProviderService outputProviderService;

    /**
     * This method is invoked when a new {@link GitRevision} has been emitted.
     *
     * @param holder
     * @param emitter
     */
    @Override
    @SneakyThrows
    public void onEmittedResource(ResourceHolder<GitRevision> holder, ResourceEmitter<GitRevision> emitter) {
        try (GitRevision resource = holder.acquire()) {
          new Builder(resource, outputProviderService).get();
        }
    }
}
