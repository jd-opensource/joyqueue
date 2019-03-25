package com.jd.journalq.toolkit.buffer.bytes;


import com.jd.journalq.toolkit.buffer.ByteArray;
import com.jd.journalq.toolkit.buffer.memory.HeapMemory;
import com.jd.journalq.toolkit.buffer.memory.HeapMemoryAllocator;
import com.jd.journalq.toolkit.buffer.memory.NativeMemory;
import com.jd.journalq.toolkit.lang.Charsets;
import com.jd.journalq.toolkit.lang.Preconditions;
import com.jd.journalq.toolkit.os.Systems;
import sun.misc.Unsafe;

/**
 * Java heap bytes.
 */
public class HeapBytes extends AbstractBytes {

    protected static final Unsafe UNSAFE = Systems.UNSAFE;
    protected static final String MAX_SIZE_ERROR_MESSAGE =
            "size cannot for HeapBytes cannot be greater than " + HeapMemory.SIZE_MAX;

    /**
     * Allocates a new heap byte array.
     * <p>
     * When the array is constructed, {@link HeapMemoryAllocator} will be used to allocate
     * {@code count} bytes on the Java heap.
     *
     * @param size The count of the buffer to allocate (in bytes).
     * @return The heap buffer.
     * @throws IllegalArgumentException If {@code count} is greater than the maximum allowed count for
     *                                  an array on the Java heap - {@code Integer.MAX_VALUE - 5}
     */
    public static HeapBytes allocate(final long size) {
        Preconditions.checkArgument(size <= HeapMemory.SIZE_MAX, MAX_SIZE_ERROR_MESSAGE);
        return new HeapBytes(HeapMemory.allocate(size));
    }

    /**
     * Wraps the given bytes in a {@link HeapBytes} object.
     * <p>
     * The returned {@link Bytes} object will be backed by a {@link HeapMemory} instance that
     * wraps the given byte array. The {@link Bytes#size()} will be equivalent to the provided
     * by array {@code length}.
     *
     * @param bytes The bytes to wrap.
     */
    public static HeapBytes wrap(final byte[] bytes) {
        return new HeapBytes(HeapMemory.wrap(bytes));
    }

    protected HeapMemory memory;

    public HeapBytes(final HeapMemory memory) {
        this.memory = memory;
    }

    public HeapMemory memory() {
        return memory;
    }

    @Override
    public ByteArray array(final boolean read) {
        return new ByteArray(memory.memory());
    }

    /**
     * Copies the bytes to a new byte array.
     *
     * @return A new {@link HeapBytes} instance backed by a copy of this instance's array.
     */
    public HeapBytes copy() {
        return new HeapBytes(memory.copy());
    }

    /**
     * Resets the heap byte array.
     *
     * @param array The internal byte array.
     * @return The heap bytes.
     */
    public HeapBytes reset(final byte[] array) {
        memory.reset(array);
        return this;
    }

    @Override
    public long size() {
        return memory.size();
    }

    @Override
    public Bytes resize(final long newSize) {
        this.memory = memory.allocator().reallocate(memory, newSize);
        return this;
    }

    @Override
    public Bytes zero() {
        return zero(0, memory.size());
    }

    @Override
    public Bytes zero(final long offset) {
        long length = memory.size() - offset;
        checkWrite(offset, length);
        return zero(offset, length);
    }

    @Override
    public Bytes zero(final long offset, final long length) {
        checkWrite(offset, length);
        byte[] array = memory.memory();
        long start = memory.address(offset);
        for (long i = start; i < length; i++) {
            array[(int) i] = 0;
        }
        return this;
    }

    @Override
    public long read(final long position, final Bytes bytes, final long offset, final long length) {
        Preconditions.checkNotNull(bytes, "bytes cannot be null");
        Preconditions.checkElementIndex((int) offset, (int) bytes.size(), "offset is invalid");
        // 可读取字节数
        long size = Math.min(checkAvailableRead(position, length), bytes.size() - offset);
        if (size <= 0) {
            return 0;
        }

        // TODO 需要测试
        if (bytes instanceof HeapBytes) {
            HeapMemory target = ((HeapBytes) bytes).memory;
            UNSAFE.copyMemory(memory.memory(), memory.address(position), target.memory(), target.address(offset), size);
        } else if (bytes instanceof NativeBytes) {
            NativeMemory target = ((NativeBytes) bytes).memory();
            UNSAFE.copyMemory(memory.memory(), memory.address(position), null, target.address(offset), size);
        } else {
            ByteArray array = bytes.array(true);
            if (array != null) {
                // 支持数组拷贝
                System.arraycopy(memory.memory(), (int) position, array.array(), (int) (array.offset() + offset),
                        (int) size);
            } else {
                for (int i = 0; i < size; i++) {
                    bytes.writeByte(offset + i, memory.getByte(position + i));
                }
            }
        }
        return size;
    }

    @Override
    public long read(final long position, final byte[] bytes, final long offset, final long length) {
        Preconditions.checkNotNull(bytes, "bytes cannot be null");
        Preconditions.checkElementIndex((int) offset, bytes.length, "offset is invalid");
        // 可读取字节数
        long size = Math.min(checkAvailableRead(position, length), length - offset);
        if (size <= 0) {
            return 0;
        }
        UNSAFE.copyMemory(memory.memory(), memory.address(position), bytes, memory.address(offset), size);
        return size;
    }

    @Override
    public int readByte(final long offset) {
        checkRead(offset, BYTE);
        return memory.getByte(offset);
    }

    @Override
    public int readUnsignedByte(final long offset) {
        checkRead(offset, BYTE);
        return memory.getByte(offset) & 0xFF;
    }

    @Override
    public char readChar(final long offset) {
        checkRead(offset, CHARACTER);
        return memory.getChar(offset);
    }

    @Override
    public short readShort(final long offset) {
        checkRead(offset, SHORT);
        return memory.getShort(offset);
    }

    @Override
    public int readUnsignedShort(final long offset) {
        checkRead(offset, SHORT);
        return memory.getShort(offset) & 0xFFFF;
    }

    @Override
    public int readMedium(final long offset) {
        checkRead(offset, MEDIUM);
        return (memory.getByte(offset)) << 16 | (memory.getByte(offset + 1) & 0xff) << 8 | (memory
                .getByte(offset + 2) & 0xff);
    }

    @Override
    public int readUnsignedMedium(final long offset) {
        checkRead(offset, MEDIUM);
        return (memory.getByte(offset) & 0xff) << 16 | (memory.getByte(offset + 1) & 0xff) << 8 | (memory
                .getByte(offset + 2) & 0xff);
    }

    @Override
    public int readInt(final long offset) {
        checkRead(offset, INTEGER);
        return memory.getInt(offset);
    }

    @Override
    public long readUnsignedInt(final long offset) {
        checkRead(offset, INTEGER);
        return memory.getInt(offset) & 0xFFFFFFFFL;
    }

    @Override
    public long readLong(final long offset) {
        checkRead(offset, LONG);
        return memory.getLong(offset);
    }

    @Override
    public float readFloat(final long offset) {
        checkRead(offset, FLOAT);
        return memory.getFloat(offset);
    }

    @Override
    public double readDouble(final long offset) {
        checkRead(offset, DOUBLE);
        return memory.getDouble(offset);
    }

    @Override
    public boolean readBoolean(final long offset) {
        checkRead(offset, BOOLEAN);
        return memory.getByte(offset) == (byte) 1;
    }

    @Override
    public String readString(final long offset) {
        // 第一个字节表示是否是空字符
        if (readByte(offset) != 0) {
            byte[] bytes = new byte[readUnsignedShort(offset + BYTE)];
            // 后面2个字节表示长度
            read(offset + MEDIUM, bytes, 0, bytes.length);
            return new String(bytes);
        }
        return null;
    }

    @Override
    public String readUTF8(final long offset) {
        // 第一个字节表示是否是空字符
        if (readByte(offset) != 0) {
            byte[] bytes = new byte[readUnsignedShort(offset + BYTE)];
            // 后面2个字节表示长度
            read(offset + MEDIUM, bytes, 0, bytes.length);
            return new String(bytes, Charsets.UTF_8);
        }
        return null;
    }

    @Override
    public Bytes write(final long position, final Bytes bytes, final long offset, final long length) {
        checkWrite(position, length);
        Preconditions.checkArgument(bytes.size() < length, "length is greater than provided byte array size");

        // TODO 测试
        if (bytes instanceof HeapBytes) {
            HeapMemory target = ((HeapBytes) bytes).memory;
            UNSAFE.copyMemory(target.memory(), target.address(offset), memory.memory(), memory.address(position),
                    length);
        } else if (bytes instanceof NativeBytes) {
            NativeMemory target = ((NativeBytes) bytes).memory;
            UNSAFE.copyMemory(null, target.address(offset), memory.memory(), memory.address(position), length);
        } else {
            ByteArray array = bytes.array(true);
            if (array != null) {
                // 支持数组拷贝
                System.arraycopy(array.array(), (int) (array.offset() + offset), memory.memory(), (int) position,
                        (int) length);
            } else {
                for (int i = 0; i < length; i++) {
                    memory.putByte(position + i, (byte) bytes.readByte(offset + i));
                }
            }
        }

        return this;
    }

    @Override
    public Bytes write(final long position, final byte[] bytes, final long offset, final long length) {
        checkWrite(position, length);
        if (bytes.length < length) {
            throw new IllegalArgumentException("length is greater than provided byte array length");
        }
        UNSAFE.copyMemory(bytes, memory.address(offset), memory.memory(), memory.address(position), length);
        return this;
    }

    @Override
    public Bytes writeByte(final long offset, final int b) {
        checkWrite(offset, BYTE);
        memory.putByte(offset, (byte) b);
        return this;
    }

    @Override
    public Bytes writeUnsignedByte(final long offset, final int b) {
        checkWrite(offset, BYTE);
        memory.putByte(offset, (byte) b);
        return this;
    }

    @Override
    public Bytes writeChar(final long offset, final char c) {
        checkWrite(offset, CHARACTER);
        memory.putChar(offset, c);
        return this;
    }

    @Override
    public Bytes writeShort(final long offset, final short s) {
        checkWrite(offset, SHORT);
        memory.putShort(offset, s);
        return this;
    }

    @Override
    public Bytes writeUnsignedShort(final long offset, final int s) {
        checkWrite(offset, SHORT);
        memory.putShort(offset, (short) s);
        return this;
    }

    @Override
    public Bytes writeMedium(final long offset, final int m) {
        memory.putByte(offset, (byte) (m >>> 16));
        memory.putByte(offset + 1, (byte) (m >>> 8));
        memory.putByte(offset + 2, (byte) m);
        return this;
    }

    @Override
    public Bytes writeUnsignedMedium(final long offset, final int m) {
        return writeMedium(offset, m);
    }

    @Override
    public Bytes writeInt(final long offset, final int i) {
        checkWrite(offset, INTEGER);
        memory.putInt(offset, i);
        return this;
    }

    @Override
    public Bytes writeUnsignedInt(final long offset, final long i) {
        checkWrite(offset, INTEGER);
        memory.putInt(offset, (int) i);
        return this;
    }

    @Override
    public Bytes writeLong(final long offset, final long l) {
        checkWrite(offset, LONG);
        memory.putLong(offset, l);
        return this;
    }

    @Override
    public Bytes writeFloat(final long offset, final float f) {
        checkWrite(offset, FLOAT);
        memory.putFloat(offset, f);
        return this;
    }

    @Override
    public Bytes writeDouble(final long offset, final double d) {
        checkWrite(offset, DOUBLE);
        memory.putDouble(offset, d);
        return this;
    }

    @Override
    public Bytes writeBoolean(final long offset, final boolean b) {
        checkWrite(offset, BOOLEAN);
        memory.putByte(offset, b ? (byte) 1 : (byte) 0);
        return this;
    }

    @Override
    public Bytes writeString(final long offset, final String s) {
        if (s == null) {
            // 空字符串写一个字节0
            return writeByte(offset, 0);
        } else {
            // 否则第一个字节写1，后面写长度，再写数据
            byte[] bytes = s.getBytes();
            return writeByte(offset, 1).writeUnsignedShort(offset + BYTE, bytes.length)
                    .write(offset + MEDIUM, bytes, 0, bytes.length);
        }
    }

    @Override
    public Bytes writeUTF8(final long offset, final String s) {
        if (s == null) {
            // 空字符串写一个字节0
            return writeByte(offset, 0);
        } else {
            byte[] bytes = s.getBytes(Charsets.UTF_8);
            // 否则第一个字节写1，后面写长度，再写数据
            return writeByte(offset, 1).writeUnsignedShort(offset + BYTE, bytes.length)
                    .write(offset + MEDIUM, bytes, 0, bytes.length);
        }
    }

    @Override
    public Bytes flush() {
        return this;
    }

    @Override
    public void close() {
        flush();
        super.close();
    }
}
