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

import com.jd.journalq.toolkit.buffer.bytes.DirectBytes;
import com.jd.journalq.toolkit.buffer.memory.DirectMemory;
import com.jd.journalq.toolkit.buffer.memory.DirectMemoryAllocator;
import com.jd.journalq.toolkit.buffer.memory.Memory;
import com.jd.journalq.toolkit.lang.Preconditions;
import com.jd.journalq.toolkit.ref.ReferenceManager;

/**
 * Direct {@link java.nio.ByteBuffer} based buffer.
 */
public class DirectBuffer extends NativeBuffer {

    /**
     * Allocates a direct buffer with an initial capacity of {@code 4096} and a maximum capacity of
     * {@link Long#MAX_VALUE}.
     * <p>
     * When the buffer is constructed, {@link DirectMemoryAllocator} will be used to allocate
     * {@code capacity} bytes of off-heap memory. The resulting buffer will be initialized with a capacity of {@code
     * 4096}
     * and have a maximum capacity of {@link Long#MAX_VALUE}. The buffer's {@code capacity} will dynamically expand as
     * bytes are written to the buffer. The underlying {@link DirectBytes} will be initialized to the next power of
     * {@code 2}.
     *
     * @return The direct buffer.
     * @see DirectBuffer#allocate(long)
     * @see DirectBuffer#allocate(long, long)
     */
    public static DirectBuffer allocate() {
        return allocate(Memory.SIZE_4K, Long.MAX_VALUE);
    }

    /**
     * Allocates a direct buffer with the given initial capacity.
     * <p>
     * When the buffer is constructed, {@link DirectMemoryAllocator} will be used to allocate
     * {@code capacity} bytes of off-heap memory. The resulting buffer will have an initial capacity of {@code
     * capacity}.
     * The underlying {@link DirectBytes} will be initialized to the next power of {@code 2}.
     *
     * @param initialCapacity The initial capacity of the buffer to allocate (in bytes).
     * @return The direct buffer.
     * @throws IllegalArgumentException If {@code capacity} is greater than the maximum allowed count for
     *                                  a {@link java.nio.ByteBuffer} - {@code Integer.MAX_VALUE - 5}
     * @see DirectBuffer#allocate()
     * @see DirectBuffer#allocate(long, long)
     */
    public static DirectBuffer allocate(final long initialCapacity) {
        return allocate(initialCapacity, Long.MAX_VALUE);
    }

    /**
     * Allocates a new direct buffer.
     * <p>
     * When the buffer is constructed, {@link DirectMemoryAllocator} will be used to allocate
     * {@code capacity} bytes of off-heap memory. The resulting buffer will have an initial capacity of {@code
     * initialCapacity}
     * and will be doubled up to {@code maxCapacity} as bytes are written to the buffer. The underlying
     * {@link DirectBytes}
     * will be initialized to the next power of {@code 2}.
     *
     * @param initialCapacity The initial capacity of the buffer to allocate (in bytes).
     * @param maxCapacity     The maximum capacity of the buffer.
     * @return The direct buffer.
     * @throws IllegalArgumentException If {@code capacity} or {@code maxCapacity} is greater than the maximum
     *                                  allowed count for a {@link java.nio.ByteBuffer} - {@code Integer.MAX_VALUE - 5}
     * @see DirectBuffer#allocate()
     * @see DirectBuffer#allocate(long)
     */
    public static DirectBuffer allocate(final long initialCapacity, final long maxCapacity) {
        Preconditions.checkArgument(initialCapacity <= maxCapacity,
                "initial capacity cannot be greater than maximum capacity");
        return new DirectBuffer(new DirectBytes(DirectMemory.allocate(Memory.Util.toPow2(initialCapacity))), 0,
                initialCapacity, maxCapacity);
    }

    protected DirectBuffer(final DirectBytes bytes, final long offset, final long initialCapacity,
            final long maxCapacity) {
        super(bytes, offset, initialCapacity, maxCapacity);
    }

    protected DirectBuffer(final DirectBytes bytes, final ReferenceManager<Buffer> referenceManager) {
        super(bytes, referenceManager);
    }

}
