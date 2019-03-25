package com.jd.journalq.toolkit.buffer;


import com.jd.journalq.toolkit.ref.ReferenceFactory;
import com.jd.journalq.toolkit.ref.ReferencePool;

/**
 * Buffer pool.
 */
public class BufferPool extends ReferencePool<Buffer> {

    public BufferPool(final ReferenceFactory<Buffer> factory) {
        super(factory);
    }

}
