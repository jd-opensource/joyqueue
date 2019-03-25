package com.jd.journalq.store.file;

import java.nio.ByteBuffer;

/**
 * @author liyue25
 * Date: 2019-01-04
 */
public interface BufferReader<T> {
    T read(ByteBuffer byteBuffer, int length);
}
