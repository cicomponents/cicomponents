/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.cicomponents.git.impl;

import lombok.Getter;
import lombok.Setter;
import org.cicomponents.common.AbstractResourceEmitter;
import org.cicomponents.git.GitRevision;
import org.cicomponents.git.GitRevisionEmitter;

public class AbstractGitMonitor extends AbstractResourceEmitter<GitRevision>
        implements GitRevisionEmitter {

    @Getter @Setter
    private Environment environment;

}
