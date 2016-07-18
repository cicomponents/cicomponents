/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.cicomponents;

/**
 * PersistentMapImplementation, unlike {@link PersistentMap} is the
 * interface to be implemented by actual implementation of a persistence
 * map so that they can be easily swapped in the singleton {@link PersistentMap} service
 * implementation.
 */
public interface PersistentMapImplementation extends PersistentMap {
}
