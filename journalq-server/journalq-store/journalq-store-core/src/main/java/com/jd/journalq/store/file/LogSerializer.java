package com.jd.journalq.store.file;

import java.nio.ByteBuffer;

/**
 * @author liyue25
 * Date: 2018-11-27
 */
public interface LogSerializer<T> extends BufferAppender<T>, BufferReader<T>{
    int size(T t);
    int trim(ByteBuffer byteBuffer, int length);
}
