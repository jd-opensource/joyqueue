package com.jd.journalq.toolkit.buffer;

import com.jd.journalq.toolkit.buffer.bytes.DirectBytes;
import com.jd.journalq.toolkit.ref.ReferenceFactory;
import com.jd.journalq.toolkit.ref.ReferenceManager;

/**
 * Direct buffer pool.
 */
public class DirectBufferPool extends BufferPool {

    public DirectBufferPool() {
        super(DirectBufferFactory.INSTANCE);
    }

    @Override
    public void release(final Buffer reference) {
        reference.rewind();
        super.release(reference);
    }

    /**
     * Direct buffer factory.
     */
    static class DirectBufferFactory implements ReferenceFactory<Buffer> {
        // 单例
        protected static final DirectBufferFactory INSTANCE = new DirectBufferFactory();

        @Override
        public Buffer create(final ReferenceManager<Buffer> manager) {
            DirectBuffer buffer = new DirectBuffer(DirectBytes.allocate(1024), manager);
            buffer.reset(0, 1024, Long.MAX_VALUE);
            return buffer;
        }
    }

}
