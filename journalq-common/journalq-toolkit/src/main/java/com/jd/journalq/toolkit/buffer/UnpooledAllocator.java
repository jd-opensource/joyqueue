package com.jd.journalq.toolkit.buffer;

/**
 * Unpooled buffer allocator.
 */
public abstract class UnpooledAllocator implements BufferAllocator {

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
        return allocate(capacity, capacity);
    }

}
