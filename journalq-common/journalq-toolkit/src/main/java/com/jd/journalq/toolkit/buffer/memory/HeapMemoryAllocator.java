package com.jd.journalq.toolkit.buffer.memory;

import com.jd.journalq.toolkit.lang.Preconditions;
import com.jd.journalq.toolkit.os.Systems;

/**
 * Java heap memory allocator.
 */
public class HeapMemoryAllocator implements MemoryAllocator<HeapMemory> {

    public static final HeapMemoryAllocator INSTANCE = new HeapMemoryAllocator();

    @Override
    public HeapMemory allocate(final long size) {
        Preconditions.checkArgument(size >= 0 && size <= Integer.MAX_VALUE, HeapMemory.SIZE_ERROR);
        return new HeapMemory(new byte[(int) size], this);
    }

    @Override
    public HeapMemory reallocate(final HeapMemory memory, final long size) {
        HeapMemory copy = allocate(size);
        Systems.UNSAFE
                .copyMemory(memory.memory(), HeapMemory.ARRAY_BASE_OFFSET, copy.memory(), HeapMemory.ARRAY_BASE_OFFSET,
                        Math.min(size, memory.size()));
        memory.free();
        return copy;
    }

}
