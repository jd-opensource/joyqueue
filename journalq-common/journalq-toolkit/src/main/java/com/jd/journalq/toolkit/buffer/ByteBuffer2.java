/**
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
package com.jd.journalq.toolkit.buffer;

import com.jd.journalq.toolkit.buffer.bytes.ByteBufferBytes;
import com.jd.journalq.toolkit.buffer.memory.Memory;
import com.jd.journalq.toolkit.lang.Preconditions;
import com.jd.journalq.toolkit.ref.Reference;
import com.jd.journalq.toolkit.ref.ReferenceManager;

import java.nio.ByteBuffer;

/**
 * Heap byte buffer implementation.
 */
public class ByteBuffer2 extends AbstractBuffer {

    /**
     * Allocates a ByteBuffer buffer with a maximum capacity of
     * {@link Memory#SIZE_MAX}.
     *
     * @return The ByteBuffer buffer.
     * @see ByteBuffer2#allocate(long)
     */
    public static ByteBuffer2 allocate() {
        return allocate(Memory.SIZE_1K);
    }

    /**
     * Allocates a new ByteBuffer buffer.
     *
     * @param capacity The capacity of the buffer.
     * @return The heap buffer.
     * @throws IllegalArgumentException If {@code capacity} is greater than the
     *                                  maximum allowed count for an array on the Java heap - {@code Integer.MAX_VALUE
     *                                  - 5}
     * @see ByteBuffer2#allocate()
     * @see ByteBuffer2#allocate(long)
     */
    public static ByteBuffer2 allocate(final long capacity) {
        return allocate(capacity, capacity);
    }

    /**
     * Allocates a new heap buffer.
     * <p>
     * The resulting buffer will have an initial capacity of
     * {@code initialCapacity} and will be doubled up to {@code maxCapacity} as bytes are written to the buffer. The
     * underlying {@link ByteBufferBytes} will be initialized to the next power of {@code 2}.
     *
     * @param initialCapacity The initial capacity of the buffer to allocate (in bytes).
     * @param maxCapacity     The maximum capacity of the buffer.
     * @return The heap buffer.
     * @throws IllegalArgumentException If {@code initialCapacity} or {@code maxCapacity} is greater than the
     *                                  maximum allowed count for an array on the Java heap - {@code Integer.MAX_VALUE
     *                                  - 5}
     * @see HeapBuffer#allocate()
     * @see HeapBuffer#allocate(long)
     */
    public static ByteBuffer2 allocate(final long initialCapacity, final long maxCapacity) {
        Preconditions.checkArgument(initialCapacity <= maxCapacity,
                "initial capacity cannot be greater than maximum capacity");
        Preconditions.checkArgument(initialCapacity <= Memory.SIZE_MAX, INITIAL_CAPACITY_OVERFLOW);
        Preconditions.checkArgument(maxCapacity <= Memory.SIZE_MAX, MAX_CAPACITY_OVERFLOW);
        return new ByteBuffer2(ByteBufferBytes.allocate(initialCapacity), 0, initialCapacity, maxCapacity);
    }

    /**
     * Wraps the given bytes in a heap buffer.
     * <p>
     * The buffer will be created with an initial capacity and maximum capacity equal to the byte array count.
     *
     * @param bytes The bytes to wrap.
     * @return The wrapped bytes.
     */
    public static ByteBuffer2 wrap(final ByteBuffer bytes) {
        Preconditions.checkNotNull(bytes, "bytes cannot be null");
        return new ByteBuffer2(ByteBufferBytes.wrap(bytes));
    }

    public ByteBuffer2(ByteBufferBytes bytes) {
        super(bytes);
    }

    public ByteBuffer2(ByteBufferBytes bytes, ReferenceManager<Buffer> referenceManager) {
        super(bytes, referenceManager);
    }

    public ByteBuffer2(ByteBufferBytes bytes, Reference reference) {
        super(bytes, reference);
    }

    public ByteBuffer2(ByteBufferBytes bytes, Reference reference, ReferenceManager<Buffer> referenceManager) {
        super(bytes, reference, referenceManager);
    }

    public ByteBuffer2(ByteBufferBytes bytes, long offset, long initialCapacity, long maxCapacity) {
        super(bytes, offset, initialCapacity, maxCapacity);
    }

    public ByteBuffer2(ByteBufferBytes bytes, long offset, long initialCapacity, long maxCapacity, Reference reference,
            ReferenceManager<Buffer> referenceManager) {
        super(bytes, offset, initialCapacity, maxCapacity, reference, referenceManager);
    }

    @Override
    protected void compact(final long from, final long to, final long length) {
        if (to != 0) {
            throw new UnsupportedOperationException("ByteBuffer only compact to zero");
        }
        ByteBuffer buffer = ((ByteBufferBytes) bytes).memory();
        buffer.limit((int) (from + length));
        buffer.position((int) from);
        buffer.compact();
    }
}
