package com.jd.journalq.toolkit.buffer.stream;

import com.jd.journalq.toolkit.buffer.BufferInput;

import java.io.IOException;
import java.io.InputStream;

/**
 * Buffer input stream.
 */
public class BufferInputStream extends InputStream {
    protected final BufferInput buffer;

    public BufferInputStream(final BufferInput buffer) {
        this.buffer = buffer;
    }

    @Override
    public int read() throws IOException {
        if (buffer.hasRemaining()) {
            return buffer.readByte();
        }
        return -1;
    }

    @Override
    public int read(final byte[] b) throws IOException {
        if (buffer.hasRemaining()) {
            int read = Math.min(b.length, (int) buffer.remaining());
            buffer.read(b);
            return read;
        }
        return -1;
    }

    @Override
    public int read(final byte[] b, int off, int len) throws IOException {
        int read = Math.min(len, (int) buffer.remaining());
        buffer.read(b, off, read);
        return read;
    }

    @Override
    public long skip(final long n) throws IOException {
        long skipped = Math.min(n, buffer.remaining());
        buffer.skip(skipped);
        return skipped;
    }

    @Override
    public int available() throws IOException {
        return (int) buffer.remaining();
    }

    @Override
    public synchronized void mark(final int readlimit) {
        throw new UnsupportedOperationException();
    }

    @Override
    public synchronized void reset() throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean markSupported() {
        return false;
    }

    @Override
    public void close() throws IOException {
        buffer.close();
    }

}
