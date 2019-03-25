package com.jd.journalq.toolkit.buffer;

/**
 * Buffer allocator.
 */
public interface BufferAllocator {

    /**
     * Allocates a dynamic capacity buffer.
     *
     * @return The allocated buffer.
     */
    Buffer allocate();

    /**
     * Allocates a dynamic capacity buffer with the given initial capacity.
     *
     * @param initialCapacity The initial buffer capacity.
     * @return The allocated buffer.
     */
    Buffer allocate(long initialCapacity);

    /**
     * Allocates a new buffer.
     *
     * @param initialCapacity The initial buffer capacity.
     * @param maxCapacity     The maximum buffer capacity.
     * @return The allocated buffer.
     */
    Buffer allocate(long initialCapacity, long maxCapacity);

}
