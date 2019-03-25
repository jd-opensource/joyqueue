package com.jd.journalq.store.file;

import java.nio.ByteBuffer;

/**
 * @author liyue25
 * Date: 2019-01-07
 */
public interface BufferAppender<T> {
    int append(T t, ByteBuffer dest);
}
