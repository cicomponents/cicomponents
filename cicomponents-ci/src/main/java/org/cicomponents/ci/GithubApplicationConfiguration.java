/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.cicomponents.ci;


/**
 * Type-safe configuration for {@link GithubApplication}
 */
public @interface GithubApplicationConfiguration {
    String clientId() default "";
    String clientSecret() default "";
    String repositories() default "";
}
