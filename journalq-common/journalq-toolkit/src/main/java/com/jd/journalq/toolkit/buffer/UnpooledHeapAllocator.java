package com.jd.journalq.toolkit.buffer;

import com.jd.journalq.toolkit.buffer.memory.HeapMemory;

/**
 * Unpooled heap allocator.
 */
public class UnpooledHeapAllocator extends UnpooledAllocator {

    @Override
    protected long maxCapacity() {
        return HeapMemory.SIZE_MAX;
    }

    @Override
    public Buffer allocate(final long initialCapacity, final long maxCapacity) {
        return HeapBuffer.allocate(initialCapacity, maxCapacity);
    }

}
