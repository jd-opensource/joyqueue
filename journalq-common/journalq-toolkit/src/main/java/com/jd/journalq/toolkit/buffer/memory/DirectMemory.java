package com.jd.journalq.toolkit.buffer.memory;

/**
 * Direct memory.
 */
public class DirectMemory extends NativeMemory<DirectMemory, DirectMemoryAllocator> {

    /**
     * Allocates direct memory via {@link DirectMemoryAllocator}.
     *
     * @param size The count of the memory to allocate.
     * @return The allocated memory.
     */
    public static DirectMemory allocate(final long size) {
        return DirectMemoryAllocator.INSTANCE.allocate(size);
    }

    public DirectMemory(final long address, final long size, final DirectMemoryAllocator allocator) {
        super(address, size, allocator);
    }

}
