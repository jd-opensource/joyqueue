package com.jd.journalq.toolkit.buffer;


import com.jd.journalq.toolkit.ref.ReferencePool;

/**
 * Pooled buffer allocator.
 */
public abstract class PooledAllocator implements BufferAllocator {
    private final ReferencePool<AbstractBuffer> pool;

    protected PooledAllocator(ReferencePool<AbstractBuffer> pool) {
        this.pool = pool;
    }

    /**
     * Returns the maximum buffer capacity.
     *
     * @return The maximum buffer capacity.
     */
    protected abstract long maxCapacity();

    @Override
    public Buffer allocate() {
        return allocate(4096, maxCapacity());
    }

    @Override
    public Buffer allocate(final long capacity) {
        return allocate(capacity, maxCapacity());
    }

    @Override
    public Buffer allocate(final long initialCapacity, final long maxCapacity) {
        return pool.acquire().reset(0, initialCapacity, maxCapacity).clear();
    }

}
