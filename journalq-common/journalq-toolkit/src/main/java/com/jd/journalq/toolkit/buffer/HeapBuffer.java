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

import com.jd.journalq.toolkit.buffer.bytes.HeapBytes;
import com.jd.journalq.toolkit.buffer.memory.HeapMemory;
import com.jd.journalq.toolkit.buffer.memory.HeapMemoryAllocator;
import com.jd.journalq.toolkit.buffer.memory.Memory;
import com.jd.journalq.toolkit.lang.Preconditions;
import com.jd.journalq.toolkit.ref.Reference;
import com.jd.journalq.toolkit.ref.ReferenceManager;

/**
 * Heap byte buffer implementation.
 */
public class HeapBuffer extends AbstractBuffer {


    /**
     * Allocates a heap buffer with an initial capacity of {@code 4096} and a maximum capacity of
     * {@link Memory#SIZE_MAX}.
     * <p>
     * When the buffer is constructed, {@link HeapMemoryAllocator} will be used to allocate
     * {@code capacity} bytes of memory on the Java heap. The resulting buffer will be initialized with a capacity of
     * {@code 4096} and have a maximum capacity of {@link Memory#SIZE_MAX}. The buffer's {@code capacity} will
     * dynamically
     * expand as bytes are written to the buffer. The underlying {@link HeapBytes} will be initialized
     * to the next power of {@code 2}.
     *
     * @return The heap buffer.
     * @see HeapBuffer#allocate(long)
     * @see HeapBuffer#allocate(long, long)
     */
    public static HeapBuffer allocate() {
        return allocate(Memory.SIZE_4K, Memory.SIZE_MAX);
    }

    /**
     * Allocates a heap buffer with the given initial capacity.
     * <p>
     * When the buffer is constructed, {@link HeapMemoryAllocator} will be used to allocate
     * {@code capacity} bytes of memory on the Java heap. The resulting buffer will have an initial capacity of {@code
     * capacity}.
     * The underlying {@link HeapBytes} will be initialized to the next power of {@code 2}.
     *
     * @param initialCapacity The initial capacity of the buffer to allocate (in bytes).
     * @return The heap buffer.
     * @throws IllegalArgumentException If {@code capacity} is greater than the maximum allowed capacity for
     *                                  an array on the Java heap - {@code Integer.MAX_VALUE - 5}
     * @see HeapBuffer#allocate()
     * @see HeapBuffer#allocate(long, long)
     */
    public static HeapBuffer allocate(final long initialCapacity) {
        return allocate(initialCapacity, Memory.SIZE_MAX);
    }

    /**
     * Allocates a new heap buffer.
     * <p>
     * When the buffer is constructed, {@link HeapMemoryAllocator} will be used to allocate
     * {@code capacity} bytes of memory on the Java heap. The resulting buffer will have an initial capacity of
     * {@code initialCapacity} and will be doubled up to {@code maxCapacity} as bytes are written to the buffer. The
     * underlying {@link HeapBytes} will be initialized to the next power of {@code 2}.
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
    public static HeapBuffer allocate(final long initialCapacity, final long maxCapacity) {
        Preconditions.checkArgument(initialCapacity <= maxCapacity,
                "initial capacity cannot be greater than maximum capacity");
        Preconditions.checkArgument(initialCapacity <= Memory.SIZE_MAX, INITIAL_CAPACITY_OVERFLOW);
        Preconditions.checkArgument(maxCapacity <= Memory.SIZE_MAX, MAX_CAPACITY_OVERFLOW);
        return new HeapBuffer(new HeapBytes(HeapMemory.allocate(Memory.Util.toPow2(initialCapacity))), 0,
                initialCapacity, maxCapacity);
    }

    /**
     * Wraps the given bytes in a heap buffer.
     * <p>
     * The buffer will be created with an initial capacity and maximum capacity equal to the byte array count.
     *
     * @param bytes The bytes to wrap.
     * @return The wrapped bytes.
     */
    public static HeapBuffer wrap(final byte[] bytes) {
        Preconditions.checkNotNull(bytes, "bytes cannot be null");
        return new HeapBuffer(HeapBytes.wrap(bytes), 0, bytes.length, bytes.length);
    }

    public HeapBuffer(HeapBytes bytes, ReferenceManager<Buffer> referenceManager) {
        super(bytes, referenceManager);
    }

    public HeapBuffer(HeapBytes bytes, Reference reference, ReferenceManager<Buffer> referenceManager) {
        super(bytes, reference, referenceManager);
    }

    public HeapBuffer(HeapBytes bytes, long offset, long initialCapacity, long maxCapacity) {
        super(bytes, offset, initialCapacity, maxCapacity);
    }

    public HeapBuffer(HeapBytes bytes, long offset, long initialCapacity, long maxCapacity, Reference reference,
            ReferenceManager<Buffer> referenceManager) {
        super(bytes, offset, initialCapacity, maxCapacity, reference, referenceManager);
    }

    @Override
    protected void compact(final long from, final long to, final long length) {
        HeapMemory memory = ((HeapBytes) bytes).memory();
        byte[] array = memory.memory();
        UNSAFE.copyMemory(array, memory.address(from), array, memory.address(to), length);
        for (long i = from; i < length; i++) {
            array[(int) i] = (byte) 0;
        }
    }

    /**
     * Resets the internal heap array.
     *
     * @param array The internal array.
     * @return The heap buffer.
     */
    public HeapBuffer reset(final byte[] array) {
        ((HeapBytes) bytes).memory().reset(array);
        clear();
        return this;
    }

}
