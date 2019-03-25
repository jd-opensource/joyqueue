package com.jd.journalq.toolkit.buffer;

import com.jd.journalq.toolkit.buffer.bytes.HeapBytes;
import com.jd.journalq.toolkit.ref.ReferenceFactory;
import com.jd.journalq.toolkit.ref.ReferenceManager;

/**
 * Heap buffer pool.
 */
public class HeapBufferPool extends BufferPool {

    public HeapBufferPool() {
        super(new HeapBufferFactory());
    }

    @Override
    public void release(final Buffer reference) {
        reference.rewind();
        super.release(reference);
    }

    /**
     * Heap buffer factory.
     */
    static class HeapBufferFactory implements ReferenceFactory<Buffer> {
        @Override
        public Buffer create(final ReferenceManager<Buffer> manager) {
            HeapBuffer buffer = new HeapBuffer(HeapBytes.allocate(1024), manager);
            buffer.reset(0, 1024, Long.MAX_VALUE);
            return buffer;
        }
    }

}
