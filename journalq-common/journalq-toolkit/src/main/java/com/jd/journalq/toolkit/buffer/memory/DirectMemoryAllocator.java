package com.jd.journalq.toolkit.buffer.memory;

import com.jd.journalq.toolkit.lang.Preconditions;
import com.jd.journalq.toolkit.os.Systems;

/**
 * Direct memory allocator.
 */
public class DirectMemoryAllocator implements MemoryAllocator<DirectMemory> {

    // 默认实例
    public static final DirectMemoryAllocator INSTANCE = new DirectMemoryAllocator();

    @Override
    public DirectMemory allocate(final long size) {
        Preconditions.checkArgument(size >= 0 && size <= Integer.MAX_VALUE, DirectMemory.SIZE_ERROR);
        DirectMemory memory = new DirectMemory(Systems.UNSAFE.allocateMemory(size), size, this);
        Systems.UNSAFE.setMemory(memory.address(), size, (byte) 0);
        return memory;
    }

    @Override
    public DirectMemory reallocate(final DirectMemory memory, final long size) {
        Preconditions.checkNotNull(memory, "memory cannot be null");
        Preconditions.checkArgument(size >= 0 && size <= Integer.MAX_VALUE, DirectMemory.SIZE_ERROR);
        DirectMemory newMemory = new DirectMemory(Systems.UNSAFE.reallocateMemory(memory.address(), size), size, this);
        if (newMemory.size() > memory.size()) {
            Systems.UNSAFE.setMemory(newMemory.address(), newMemory.size() - memory.size(), (byte) 0);
        }
        return newMemory;
    }

}
