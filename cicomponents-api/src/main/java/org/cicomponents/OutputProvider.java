/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.cicomponents;

import java.io.OutputStream;
import java.util.Date;
import java.util.stream.Stream;

public interface OutputProvider {
    interface TimestampedOutput {
        enum Kind {
            STDOUT, STDERR
        }
        Kind getKind();
        Date getDate();
        byte[] getOutput();
    }

    Stream<TimestampedOutput> getOutput();
    Date getLatestDate();

    OutputStream getStandardOutput();
    OutputStream getStandardError();

}
