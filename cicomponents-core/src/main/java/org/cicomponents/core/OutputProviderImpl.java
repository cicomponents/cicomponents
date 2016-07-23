/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.cicomponents.core;

import lombok.Getter;
import lombok.SneakyThrows;
import lombok.Synchronized;
import lombok.Value;
import org.cicomponents.OutputProvider;

import java.io.IOException;
import java.io.OutputStream;
import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class OutputProviderImpl implements OutputProvider {

    @Value
    private static class TimestampedOutputImpl implements TimestampedOutput {
        private Kind kind;
        private Date date;
        private byte[] output;
    }

    private class CollectingOutputStream extends OutputStream {
        private final TimestampedOutput.Kind kind;

        private CollectingOutputStream(TimestampedOutput.Kind kind) {this.kind = kind;}

        @Override public void write(int b) throws IOException {
            OutputProviderImpl.this.write(new TimestampedOutputImpl(kind, new Date(), new byte[]{(byte) b}));
        }

        @Override public void write(byte[] b) throws IOException {
            OutputProviderImpl.this.write(new TimestampedOutputImpl(kind, new Date(), b));
        }

        @Override public void close() throws IOException {
            OutputProviderImpl.this.close();
        }
    }

    @Getter
    private OutputStream standardOutput = new CollectingOutputStream(TimestampedOutput.Kind.STDOUT);
    @Getter
    private OutputStream standardError = new CollectingOutputStream(TimestampedOutput.Kind.STDERR);

    private List<TimestampedOutput> buffer = new ArrayList<>();

    @Getter
    private Date latestDate;

    public OutputProviderImpl() {
        latestDate = new Date();
    }

    private void write(TimestampedOutput timestampedOutput) {
        synchronized (buffer) {
            buffer.add(timestampedOutput);
            latestDate = timestampedOutput.getDate();
            buffer.notifyAll();
        }
    }

    private volatile boolean open = true;

    private void close() {
        synchronized (buffer) {
            buffer.notifyAll();
            open = false;
        }
    }

    @Synchronized("buffer")
    @Override public Stream<TimestampedOutput> getOutput() {
        Spliterator<TimestampedOutput> spliterator = Spliterators
                .spliteratorUnknownSize(new TimestampedOutputIterator(), Spliterator.CONCURRENT);
        return StreamSupport.stream(spliterator, false);
    }

    private class TimestampedOutputIterator implements Iterator<TimestampedOutput> {
        private int position = 0;

        @SneakyThrows
        @Override public boolean hasNext() {
            synchronized (buffer) {
                if (position < buffer.size() - 1) {
                    return true;
                }
                if (!open) {
                    return false;
                }
                while (true) {
                    buffer.wait();
                    if (position < buffer.size() - 1) {
                        return true;
                    }
                    if (!open) {
                        return false;
                    }
                }
            }
        }

        @Override public TimestampedOutput next() {
            synchronized (buffer) {
                TimestampedOutput output = buffer.get(position);
                position++;
                return output;
            }
        }
    }

}
