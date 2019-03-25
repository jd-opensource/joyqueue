package com.jd.journalq.toolkit.buffer;

import com.jd.journalq.toolkit.buffer.memory.HeapMemory;
import com.jd.journalq.toolkit.ref.ReferencePool;

/**
 * Pooled heap buffer allocator.
 */
public class PooledHeapAllocator extends PooledAllocator {

    public static final PooledHeapAllocator INSTANCE = new PooledHeapAllocator();

    @SuppressWarnings({"unchecked", "rawtypes"})
    public PooledHeapAllocator() {
        super((ReferencePool) new HeapBufferPool());
    }

    @Override
    protected long maxCapacity() {
        return HeapMemory.SIZE_MAX;
    }

}
