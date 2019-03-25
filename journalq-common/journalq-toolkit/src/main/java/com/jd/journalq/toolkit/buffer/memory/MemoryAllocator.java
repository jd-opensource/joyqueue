package com.jd.journalq.toolkit.buffer.memory;

import com.jd.journalq.toolkit.buffer.bytes.Bytes;

/**
 * Memory allocator.
 * <p>
 * Memory allocators handle allocation of memory for {@link Bytes} objects, providing descriptors
 * that point to memory addresses.
 */
public interface MemoryAllocator<T extends Memory> {

    /**
     * Allocates memory.
     *
     * @param size The count of the memory to allocate.
     * @return The allocated memory.
     */
    T allocate(long size);

    /**
     * Reallocates the given memory.
     * <p>
     * When the memory is reallocated, the memory address for the given {@link Memory} instance may change. The returned
     * {@link Memory} instance will contain the updated address and count.
     *
     * @param memory The memory to reallocate.
     * @param size   The count to which to reallocate the given memory.
     * @return The reallocated memory.
     */
    T reallocate(T memory, long size);

}
