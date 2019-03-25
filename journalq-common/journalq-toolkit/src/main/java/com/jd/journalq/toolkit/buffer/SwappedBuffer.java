package com.jd.journalq.toolkit.buffer;

import java.nio.ByteOrder;

/**
 * 交换字节顺序缓冲器.
 */
public class SwappedBuffer extends AbstractBuffer {
    protected final Buffer root;

    SwappedBuffer(Buffer buffer, long offset, long initialCapacity, long maxCapacity) {
        super(buffer.bytes()
                        .order(buffer.order() == ByteOrder.BIG_ENDIAN ? ByteOrder.LITTLE_ENDIAN : ByteOrder
                                .BIG_ENDIAN), offset,
                initialCapacity, maxCapacity);
        if (buffer instanceof SwappedBuffer) {
            Buffer parent = buffer;
            while (parent != null && parent instanceof SwappedBuffer) {
                parent = ((SwappedBuffer) parent).root();
            }
            this.root = parent;
        } else {
            this.root = buffer;
        }

        root.acquire();
    }

    /**
     * Returns the root buffer.
     *
     * @return The root buffer.
     */
    public Buffer root() {
        return root;
    }

    @Override
    public ByteArray array(final boolean read) {
        return root.array(read);
    }

    @Override
    public boolean isDirect() {
        return root.isDirect();
    }

    @Override
    public boolean isFile() {
        return root.isFile();
    }

    @Override
    public boolean isReadOnly() {
        return root.isReadOnly();
    }

    @Override
    public Buffer position(long position) {
        return super.position(position);
        //return root.position(position);
    }

    @Override
    protected void compact(final long from, final long to, final long length) {
        if (root instanceof AbstractBuffer) {
            ((AbstractBuffer) root).compact(from, to, length);
        }
    }

    @Override
    public void acquire() {
        root.acquire();
    }

    @Override
    public boolean release() {
        return root.release();
    }

    @Override
    public void close() {
        root.close();
    }

}
