package com.jd.journalq.toolkit.buffer;


import com.jd.journalq.toolkit.ref.ReferencePool;

/**
 * Pooled direct buffer allocator.
 */
public class PooledDirectAllocator extends PooledAllocator {

    public static final PooledDirectAllocator INSTANCE = new PooledDirectAllocator();

    @SuppressWarnings({"unchecked", "rawtypes"})
    public PooledDirectAllocator() {
        super((ReferencePool) new DirectBufferPool());
    }

    @Override
    protected long maxCapacity() {
        return Long.MAX_VALUE;
    }
}
