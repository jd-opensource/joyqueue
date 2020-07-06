/**
 * Copyright 2019 The JoyQueue Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.joyqueue.broker.kafka.message.compressor.lz4;

import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

public abstract class BufferSupplier implements AutoCloseable {

    public static final BufferSupplier NO_CACHING = new BufferSupplier() {
        @Override
        public ByteBuffer get(int capacity) {
            return ByteBuffer.allocate(capacity);
        }

        @Override
        public void release(ByteBuffer buffer) {}

        @Override
        public void close() {}
    };

    public static BufferSupplier create() {
        return new DefaultSupplier();
    }

    /**
     * Supply a buffer with the required capacity. This may return a cached buffer or allocate a new instance.
     */
    public abstract ByteBuffer get(int capacity);

    /**
     * Return the provided buffer to be reused by a subsequent call to `get`.
     */
    public abstract void release(ByteBuffer buffer);

    /**
     * Release all resources associated with this supplier.
     */
    public abstract void close();

    private static class DefaultSupplier extends BufferSupplier {
        // We currently use a single block size, so optimise for that case
        private final Map<Integer, Deque<ByteBuffer>> bufferMap = new HashMap<>(1);

        @Override
        public ByteBuffer get(int size) {
            Deque<ByteBuffer> bufferQueue = bufferMap.get(size);
            if (bufferQueue == null || bufferQueue.isEmpty())
                return ByteBuffer.allocate(size);
            else
                return bufferQueue.pollFirst();
        }

        @Override
        public void release(ByteBuffer buffer) {
            buffer.clear();
            Deque<ByteBuffer> bufferQueue = bufferMap.get(buffer.capacity());
            if (bufferQueue == null) {
                // We currently keep a single buffer in flight, so optimise for that case
                bufferQueue = new ArrayDeque<>(1);
                bufferMap.put(buffer.capacity(), bufferQueue);
            }
            bufferQueue.addLast(buffer);
        }

        @Override
        public void close() {
            bufferMap.clear();
        }
    }

    /**
     * Simple buffer supplier for single-threaded usage. It caches a single buffer, which grows
     * monotonically as needed to fulfill the allocation request.
     */
    public static class GrowableBufferSupplier extends BufferSupplier {
        private ByteBuffer cachedBuffer;

        @Override
        public ByteBuffer get(int minCapacity) {
            if (cachedBuffer != null && cachedBuffer.capacity() >= minCapacity) {
                ByteBuffer res = cachedBuffer;
                cachedBuffer = null;
                return res;
            } else {
                cachedBuffer = null;
                return ByteBuffer.allocate(minCapacity);
            }
        }

        @Override
        public void release(ByteBuffer buffer) {
            buffer.clear();
            cachedBuffer = buffer;
        }

        @Override
        public void close() {
            cachedBuffer = null;
        }
    }

}
