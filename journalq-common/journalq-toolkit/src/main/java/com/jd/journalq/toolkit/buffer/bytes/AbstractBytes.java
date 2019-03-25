package com.jd.journalq.toolkit.buffer.bytes;

import com.jd.journalq.toolkit.buffer.ByteArray;
import com.jd.journalq.toolkit.lang.Charsets;
import com.jd.journalq.toolkit.lang.Preconditions;

import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.ByteOrder;

/**
 * Abstract bytes implementation.
 * <p>
 * This class provides common state and bounds checking functionality for all {@link Bytes} implementations.
 */
public abstract class AbstractBytes implements Bytes {
    protected boolean open = true;
    protected SwappedBytes swap;

    /**
     * Checks whether the block is open.
     */
    protected void checkOpen() {
        Preconditions.checkState(open, "bytes not open");
    }

    /**
     * Checks that the offset is within the bounds of the buffer.
     *
     * @param offset 偏移位置
     * @throws IndexOutOfBoundsException
     */
    protected void checkOffset(final long offset) {
        checkOpen();
        if (offset < 0 || offset >= size()) {
            throw new IndexOutOfBoundsException();
        }
    }

    /**
     * Checks bounds for a read.
     *
     * @param offset 偏移位置
     * @param length 长度
     * @throws BufferUnderflowException
     */
    protected void checkRead(final long offset, long length) {
        checkOffset(offset);
        long position = offset + length;
        if (position > size()) {
            throw new BufferUnderflowException();
        }
    }

    /**
     * 检查并获取可以读取的数量
     *
     * @param offset 偏移位置
     * @param length 最大读取长度
     * @return 可以读取的长度
     * @throws BufferUnderflowException
     */
    protected long checkAvailableRead(final long offset, long length) {
        checkOffset(offset);
        return Math.min(length, size() - offset);
    }

    /**
     * Checks bounds for a write.
     *
     * @param offset 偏移位置
     * @param length 长度
     * @throws BufferUnderflowException
     */
    protected void checkWrite(final long offset, long length) {
        checkOffset(offset);
        long position = offset + length;
        if (position > size()) {
            throw new BufferOverflowException();
        }
    }

    @Override
    public boolean isDirect() {
        return false;
    }

    @Override
    public boolean isFile() {
        return false;
    }

    @Override
    public ByteArray array(final boolean read) {
        return null;
    }

    @Override
    public Bytes zero() {
        return zero(0, size());
    }

    @Override
    public Bytes zero(final long offset) {
        return zero(offset, size() - offset);
    }

    @Override
    public int readUnsignedByte(final long offset) {
        return readByte(offset) & 0xFF;
    }

    @Override
    public int readUnsignedShort(long offset) {
        return readShort(offset) & 0xFFFF;
    }

    @Override
    public long readUnsignedInt(final long offset) {
        return readInt(offset) & 0xFFFFFFFFL;
    }

    @Override
    public float readFloat(final long offset) {
        return Float.intBitsToFloat(readInt(offset));
    }

    @Override
    public double readDouble(final long offset) {
        return Double.longBitsToDouble(readLong(offset));
    }

    @Override
    public boolean readBoolean(final long offset) {
        checkRead(offset, BYTE);
        return readByte(offset) == (byte) 1;
    }

    @Override
    public String readString(final long offset) {
        byte[] bytes = new byte[readUnsignedShort(offset + BYTE)];
        read(offset + BYTE + SHORT, bytes, 0, bytes.length);
        return new String(bytes);
    }

    @Override
    public String readUTF8(final long offset) {
        byte[] bytes = new byte[readUnsignedShort(offset + BYTE)];
        read(offset + BYTE + SHORT, bytes, 0, bytes.length);
        return new String(bytes, Charsets.UTF_8);
    }

    @Override
    public Bytes writeUnsignedByte(final long offset, final int b) {
        return writeByte(offset, b);
    }

    @Override
    public Bytes writeUnsignedShort(final long offset, final int s) {
        return writeShort(offset, (short) s);
    }

    @Override
    public Bytes writeUnsignedMedium(final long offset, final int m) {
        return writeMedium(offset, m);
    }

    @Override
    public Bytes writeUnsignedInt(final long offset, final long i) {
        return writeInt(offset, (int) i);
    }

    @Override
    public Bytes writeFloat(final long offset, final float f) {
        return writeInt(offset, Float.floatToRawIntBits(f));
    }

    @Override
    public Bytes writeDouble(final long offset, final double d) {
        return writeLong(offset, Double.doubleToRawLongBits(d));
    }

    @Override
    public Bytes writeBoolean(final long offset, final boolean b) {
        return writeByte(offset, b ? (byte) 1 : (byte) 0);
    }

    @Override
    public Bytes writeString(final long offset, final String s) {
        byte[] bytes = s == null ? new byte[0] : s.getBytes();
        return writeUnsignedShort(offset + BYTE, bytes.length)
                .write(offset + MEDIUM, bytes, 0, bytes.length);
    }

    @Override
    public Bytes writeUTF8(final long offset, final String s) {
        byte[] bytes = s == null ? new byte[0] : s.getBytes(Charsets.UTF_8);
        return writeUnsignedShort(offset + BYTE, bytes.length)
                .write(offset + MEDIUM, bytes, 0, bytes.length);
    }

    @Override
    public ByteOrder order() {
        return ByteOrder.BIG_ENDIAN;
    }

    @Override
    public Bytes order(final ByteOrder order) {
        Preconditions.checkNotNull(order, "order cannot be null");
        if (order == order()) {
            return this;
        } else if (swap != null) {
            return swap;
        }
        swap = new SwappedBytes(this);
        return swap;
    }

    @Override
    public void close() {
        open = false;
    }

}
