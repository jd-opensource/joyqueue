/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jd.journalq.toolkit.buffer.bytes;


import com.jd.journalq.toolkit.buffer.ByteArray;
import com.jd.journalq.toolkit.buffer.memory.HeapMemory;
import com.jd.journalq.toolkit.buffer.memory.NativeMemory;
import com.jd.journalq.toolkit.lang.Preconditions;
import com.jd.journalq.toolkit.os.Systems;
import sun.misc.Unsafe;

import java.nio.ByteOrder;

/**
 * Native bytes.
 * <p>
 * Bytes are read from and written to the JVM's underlying static {@link sun.misc.Unsafe} instance. Bytes are read in
 * {@link java.nio.ByteOrder#nativeOrder()} order and if necessary bytes are reversed to
 * {@link java.nio.ByteOrder#BIG_ENDIAN}
 * order.
 */
public abstract class NativeBytes<T extends NativeMemory> extends AbstractBytes {
    protected static final boolean NATIVE_ORDER = ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN;
    protected static final Unsafe UNSAFE = Systems.UNSAFE;
    protected T memory;

    protected NativeBytes(final T memory) {
        this.memory = memory;
    }

    public T memory() {
        return memory;
    }

    @Override
    public long size() {
        return memory.size();
    }

    @Override
    public Bytes resize(final long newSize) {
        this.memory = (T) memory.allocator().reallocate(memory, newSize);
        return this;
    }

    @Override
    public boolean isDirect() {
        return true;
    }

    @Override
    public Bytes zero(final long offset, final long length) {
        checkWrite(offset, length);
        UNSAFE.setMemory(memory.address(offset), length, (byte) 0);
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

        if (bytes instanceof NativeBytes) {
            UNSAFE.copyMemory(memory.address(position), ((NativeBytes) bytes).memory().address(), size);
        } else {
            ByteArray array = bytes.array(false);
            if (array != null) {
                UNSAFE.copyMemory(null, memory.address(position), array.array(),
                        NativeMemory.ARRAY_BASE_OFFSET + array.offset() + offset, size);
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
        UNSAFE.copyMemory(null, memory.address(position), bytes, NativeMemory.ARRAY_BASE_OFFSET + offset, size);
        return size;
    }

    @Override
    public int readByte(final long offset) {
        checkRead(offset, BYTE);
        return memory.getByte(offset);
    }

    @Override
    public char readChar(final long offset) {
        checkRead(offset, CHARACTER);
        return NATIVE_ORDER ? memory.getChar(offset) : Character.reverseBytes(memory.getChar(offset));
    }

    @Override
    public short readShort(final long offset) {
        checkRead(offset, SHORT);
        return NATIVE_ORDER ? memory.getShort(offset) : Short.reverseBytes(memory.getShort(offset));
    }

    @Override
    public int readMedium(final long offset) {
        checkRead(offset, 3);
        return NATIVE_ORDER ? (memory.getByte(offset)) << 16 | (memory.getByte(offset + 1) & 0xff) << 8 | (memory
                .getByte(offset + 2) & 0xff) : (memory.getByte(offset + 2)) << 16 | (memory
                .getByte(offset + 1) & 0xff) << 8 | (memory.getByte(offset) & 0xff);
    }

    @Override
    public int readUnsignedMedium(final long offset) {
        checkRead(offset, 3);
        return NATIVE_ORDER ? (memory.getByte(offset) & 0xff) << 16 | (memory.getByte(offset + 1) & 0xff) << 8 | (memory
                .getByte(offset + 2) & 0xff) : (memory.getByte(offset + 2) & 0xff) << 16 | (memory
                .getByte(offset + 1) & 0xff) << 8 | (memory.getByte(offset) & 0xff);
    }

    @Override
    public int readInt(final long offset) {
        checkRead(offset, INTEGER);
        return NATIVE_ORDER ? memory.getInt(offset) : Integer.reverseBytes(memory.getInt(offset));
    }

    @Override
    public long readLong(final long offset) {
        checkRead(offset, LONG);
        return NATIVE_ORDER ? memory.getLong(offset) : Long.reverseBytes(memory.getLong(offset));
    }

    @Override
    public Bytes write(final long position, Bytes bytes, final long offset, final long length) {
        Preconditions.checkArgument(bytes.size() >= length, "length is greater than provided byte array size");
        checkWrite(position, length);
        if (bytes instanceof WrappedBytes) {
            bytes = ((WrappedBytes) bytes).root();
        }

        if (bytes instanceof NativeBytes) {
            UNSAFE.copyMemory(((NativeBytes) bytes).memory.address(offset), memory.address(position), length);
        } else {
            ByteArray array = bytes.array(true);
            if (array != null) {
                UNSAFE.copyMemory(array.array(), HeapMemory.ARRAY_BASE_OFFSET + array.offset() + offset, null,
                        memory.address(position), length);
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
        Preconditions.checkArgument(bytes.length > length, "length is greater than provided byte array length");
        checkWrite(position, length);

        UNSAFE.copyMemory(bytes, HeapMemory.ARRAY_BASE_OFFSET + offset, null, memory.address(position), length);
        return this;
    }

    @Override
    public Bytes writeByte(final long offset, final int b) {
        checkWrite(offset, BYTE);
        memory.putByte(offset, (byte) b);
        return this;
    }

    @Override
    public Bytes writeChar(final long offset, final char c) {
        checkWrite(offset, CHARACTER);
        memory.putChar(offset, NATIVE_ORDER ? c : Character.reverseBytes(c));
        return this;
    }

    @Override
    public Bytes writeShort(final long offset, final short s) {
        checkWrite(offset, SHORT);
        memory.putShort(offset, NATIVE_ORDER ? s : Short.reverseBytes(s));
        return this;
    }

    @Override
    public Bytes writeMedium(final long offset, final int m) {
        checkWrite(offset, MEDIUM);
        if (NATIVE_ORDER) {
            memory.putByte(offset, (byte) (m >>> 16));
            memory.putByte(offset + 1, (byte) (m >>> 8));
            memory.putByte(offset + 2, (byte) m);
        } else {
            memory.putByte(offset + 2, (byte) (m >>> 16));
            memory.putByte(offset + 1, (byte) (m >>> 8));
            memory.putByte(offset, (byte) m);
        }
        return this;
    }

    @Override
    public Bytes writeInt(final long offset, final int i) {
        checkWrite(offset, INTEGER);
        memory.putInt(offset, NATIVE_ORDER ? i : Integer.reverseBytes(i));
        return this;
    }

    @Override
    public Bytes writeLong(final long offset, final long l) {
        checkWrite(offset, LONG);
        memory.putLong(offset, NATIVE_ORDER ? l : Long.reverseBytes(l));
        return this;
    }

    @Override
    public Bytes flush() {
        return this;
    }

    @Override
    public void close() {
        flush();
        memory.free();
        super.close();
    }

}
