package com.jd.journalq.toolkit.buffer.bytes;


import com.jd.journalq.toolkit.buffer.ByteArray;
import com.jd.journalq.toolkit.buffer.memory.Memory;
import com.jd.journalq.toolkit.lang.Preconditions;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * ByteBuffer 字节对象
 */
public class ByteBufferBytes extends AbstractBytes {
    protected static final String SIZE_ERROR = "size must be in [0," + Memory.SIZE_MAX + "]";
    protected static final boolean NATIVE_ORDER = ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN;
    protected ByteBuffer memory;

    /**
     * Wraps the given bytes in a {@link ByteBufferBytes} object.
     *
     * @param bytes The bytes to wrap.
     */
    public static ByteBufferBytes wrap(final ByteBuffer bytes) {
        return new ByteBufferBytes(bytes);
    }

    /**
     * 分配内存
     *
     * @param size 内存大小
     * @return
     */
    public static ByteBufferBytes allocate(final long size) {
        Preconditions.checkArgument(size >= 0 && size <= Integer.MAX_VALUE, SIZE_ERROR);
        return new ByteBufferBytes(ByteBuffer.allocate((int) size));
    }

    public ByteBufferBytes(final ByteBuffer memory) {
        Preconditions.checkNotNull(memory, "memory cannot be null");
        this.memory = memory;
    }

    public ByteBuffer memory() {
        return memory;
    }

    @Override
    public long size() {
        return memory.capacity();
    }

    @Override
    public Bytes resize(final long newSize) {
        Preconditions.checkArgument(newSize >= 0 && newSize <= Memory.SIZE_MAX, SIZE_ERROR);
        ByteBuffer buffer = ByteBuffer.allocate((int) newSize);
        memory.flip();
        write(memory, buffer);
        this.memory = buffer;
        return this;
    }

    @Override
    public boolean isDirect() {
        return memory.isDirect();
    }

    @Override
    public ByteArray array(final boolean read) {
        if (memory.hasArray()) {
            // TODO 判断读写模式
            return new ByteArray(memory.array(), memory.arrayOffset(), memory.capacity());
        }
        return null;
    }

    @Override
    public Bytes zero(final long offset, final long length) {
        checkWrite(offset, length);
        if (memory.limit() > offset) {
            memory.limit((int) offset);
        }
        if (memory.position() > offset) {
            memory.position((int) offset);
        }
        return this;
    }

    @Override
    public long read(final long position, Bytes bytes, final long offset, final long length) {
        Preconditions.checkNotNull(bytes, "bytes cannot be null");
        Preconditions.checkElementIndex((int) offset, (int) bytes.size(), "offset is invalid");
        // 可读取字节数
        long size = Math.min(checkAvailableRead(position, length), bytes.size() - offset);
        if (size <= 0) {
            return 0;
        }
        if (bytes instanceof WrappedBytes) {
            bytes = ((WrappedBytes) bytes).root();
        }
        ByteArray src = array(true);
        ByteArray target = bytes.array(false);
        if (src != null && target != null) {
            System.arraycopy(src.array(), (int) (src.offset() + position), target.array(),
                    (int) (target.offset() + offset), (int) size);
        } else if (src != null) {
            bytes.write(offset, src.array(), src.offset() + position, size);
        } else {
            for (int i = 0; i < size; i++) {
                bytes.writeByte(offset + i, memory.get((int) (position + i)));
            }
        }
        return size;
    }

    @Override
    public long read(final long position, final byte[] bytes, final long offset, final long length) {
        Preconditions.checkNotNull(bytes, "bytes cannot be null");
        Preconditions.checkElementIndex((int) offset, bytes.length, "offset is invalid");
        // 可读取字节数
        long size = Math.min(checkAvailableRead(position, length), bytes.length - offset);
        if (size <= 0) {
            return 0;
        }
        ByteArray src = array(true);
        if (src != null) {
            System.arraycopy(src, (int) (src.offset() + position), bytes, (int) offset, (int) size);
        } else {
            for (int i = 0; i < length; i++) {
                bytes[(int) (offset + i)] = memory.get((int) (position + i));
            }
        }
        return length;
    }

    @Override
    public int readByte(final long offset) {
        return memory.get((int) offset);
    }

    @Override
    public char readChar(final long offset) {
        return memory.getChar((int) offset);
    }

    @Override
    public short readShort(final long offset) {
        return memory.getShort((int) offset);
    }

    @Override
    public int readMedium(final long offset) {
        int pos = (int) offset;
        return NATIVE_ORDER ? (memory.get(pos)) << 16 | (memory.get(pos + 1) & 0xff) << 8 | (memory
                .get(pos + 2) & 0xff) : (memory.get(pos + 2)) << 16 | (memory.get(pos + 1) & 0xff) << 8 | (memory
                .get(pos) & 0xff);
    }

    @Override
    public int readUnsignedMedium(final long offset) {
        int pos = (int) offset;
        return NATIVE_ORDER ? (memory.get(pos) & 0xff) << 16 | (memory.get(pos + 1) & 0xff) << 8 | (memory
                .get(pos + 2) & 0xff) : (memory.get(pos + 2) & 0xff) << 16 | (memory.get(pos + 1) & 0xff) << 8 | (memory
                .get(pos) & 0xff);
    }

    @Override
    public int readInt(final long offset) {
        return memory.getInt((int) offset);
    }

    @Override
    public long readLong(final long offset) {
        return memory.getLong((int) offset);
    }

    @Override
    public Bytes write(final long position, Bytes bytes, final long offset, final long length) {
        Preconditions.checkNotNull(bytes, "bytes cannot be null");
        int capacity = memory.capacity();
        if (offset < 0 || offset > bytes.size()) {
            throw new IndexOutOfBoundsException("offset must be in [0," + bytes.size() + ")");
        } else if (position < 0 || position >= capacity) {
            throw new IndexOutOfBoundsException("position must be in [0," + capacity + ")");
        } else if (length < 0 || (length + offset) > bytes.size()) {
            throw new BufferUnderflowException();
        } else if ((capacity - position) < length) {
            throw new BufferUnderflowException();
        }

        if (bytes instanceof WrappedBytes) {
            bytes = ((WrappedBytes) bytes).root();
        }

        int pos = (int) position;
        ByteArray array = bytes.array(true);
        if (array != null) {
            write(pos, array.array(), (int) (array.offset() + offset), (int) length);
        } else {
            for (int i = 0; i < length; i++) {
                memory.put(pos + i, (byte) bytes.readByte(offset + i));
            }
        }
        return this;
    }

    @Override
    public Bytes write(final long position, final byte[] bytes, final long offset, final long length) {
        Preconditions.checkNotNull(bytes, "bytes cannot be null");
        int capacity = memory.capacity();
        if (offset < 0 || offset > bytes.length) {
            throw new IndexOutOfBoundsException("offset must be in [0," + bytes.length + ")");
        } else if (position < 0 || position >= capacity) {
            throw new IndexOutOfBoundsException("position must be in [0," + capacity + ")");
        } else if (length < 0 || (length + offset) > bytes.length) {
            throw new BufferUnderflowException();
        } else if ((capacity - position) < length) {
            throw new BufferUnderflowException();
        }
        write((int) position, bytes, (int) offset, (int) length);
        return this;
    }

    @Override
    public Bytes writeByte(final long offset, final int b) {
        memory.put((int) offset, (byte) b);
        return this;
    }

    @Override
    public Bytes writeChar(final long offset, final char c) {
        memory.putChar((int) offset, c);
        return this;
    }

    @Override
    public Bytes writeShort(final long offset, final short s) {
        memory.putShort((int) offset, s);
        return this;
    }

    @Override
    public Bytes writeMedium(final long offset, final int m) {
        int pos = (int) offset;
        if (NATIVE_ORDER) {
            memory.put(pos, (byte) (m >>> 16));
            memory.put(pos + 1, (byte) (m >>> 8));
            memory.put(pos + 2, (byte) m);
        } else {
            memory.put(pos + 2, (byte) (m >>> 16));
            memory.put(pos + 1, (byte) (m >>> 8));
            memory.put(pos, (byte) m);
        }
        return this;
    }

    @Override
    public Bytes writeInt(final long offset, final int i) {
        memory.putInt((int) offset, i);
        return this;
    }

    @Override
    public Bytes writeLong(final long offset, final long l) {
        memory.putLong((int) offset, l);
        return this;
    }

    @Override
    public Bytes flush() {
        return this;
    }

    @Override
    public void close() {
        flush();
        memory.clear();
        super.close();
    }

    /**
     * 从目标缓冲器拷贝数据到当前内存
     *
     * @param src 目标内存
     */
    protected void write(final ByteBuffer src, final ByteBuffer target) {
        int length = src.remaining();
        // 都是数组
        if (src.hasArray() && target.hasArray()) {
            System.arraycopy(src.array(), src.arrayOffset() + src.position(), target.array(),
                    target.arrayOffset() + target.position(), length);
        } else {
            for (int i = 0; i < length; i++) {
                // 写入并修改当前内存位置
                target.put(src.get(i));
            }
        }
    }

    /**
     * 从目标内存数据拷贝到当前内存
     *
     * @param position 写入位置
     * @param src      目标内存
     * @param length   最大长度
     * @param offset   数组中的偏移量
     */
    protected void write(final int position, final ByteBuffer src, final int offset, final int length) {
        // 都是数组
        if (src.hasArray() && memory.hasArray()) {
            System.arraycopy(src, src.arrayOffset() + offset, memory.array(), memory.arrayOffset() + position, length);
        } else {
            for (int i = 0; i < length; i++) {
                // 写入并修改当前内存位置
                memory.put(position + i, src.get(i));
            }
        }
    }

    /**
     * 从目标内存数据拷贝到当前内存
     *
     * @param position 写入位置
     * @param array    数组
     * @param length   最大长度
     * @param offset   数组中的偏移量
     */
    protected void write(final int position, final byte[] array, final int offset, final int length) {
        // 都是数组
        if (memory.hasArray()) {
            System.arraycopy(array, offset, memory.array(), memory.arrayOffset() + position, length);
        } else {
            for (int i = 0; i < length; i++) {
                memory.put(position + i, array[i]);
            }
        }
    }

}
