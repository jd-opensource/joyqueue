package com.jd.journalq.store.file;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

public interface StoreFile<T> extends Timed {
    File file();
    long position();
    boolean unload();
    boolean hasPage();
    T read(int position, int length) throws IOException;
    int append(T t) throws IOException;
    ByteBuffer readByteBuffer(int position, int length) throws IOException;
    int appendByteBuffer(ByteBuffer byteBuffer) throws IOException;
    int flush() throws IOException;
    void rollback(int position) throws IOException;
    boolean isClean();
    int writePosition();
    int fileDataSize();
    int flushPosition();
    long timestamp();
}
