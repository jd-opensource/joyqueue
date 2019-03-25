package com.jd.journalq.toolkit.buffer.stream;

import com.jd.journalq.toolkit.buffer.BufferOutput;

import java.io.IOException;
import java.io.OutputStream;

/**
 * 缓冲器输出流
 */
public class BufferOutputStream extends OutputStream {
    protected final BufferOutput<?> buffer;

    public BufferOutputStream(BufferOutput<?> buffer) {
        this.buffer = buffer;
    }

    @Override
    public void write(final int b) throws IOException {
        buffer.writeByte(b);
    }

    @Override
    public void write(final byte[] b) throws IOException {
        buffer.write(b);
    }

    @Override
    public void write(final byte[] b, final int off, final int len) throws IOException {
        buffer.write(b, off, len);
    }

    @Override
    public void flush() throws IOException {
        buffer.flush();
    }

    @Override
    public void close() throws IOException {
        buffer.close();
    }

}
