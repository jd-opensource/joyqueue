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
package com.jd.journalq.toolkit.buffer;


import com.jd.journalq.toolkit.buffer.bytes.Bytes;
import com.jd.journalq.toolkit.buffer.memory.Memory;
import com.jd.journalq.toolkit.lang.Charsets;
import com.jd.journalq.toolkit.lang.Preconditions;
import com.jd.journalq.toolkit.os.Systems;
import com.jd.journalq.toolkit.ref.Reference;
import com.jd.journalq.toolkit.ref.ReferenceCounter;
import com.jd.journalq.toolkit.ref.ReferenceManager;
import sun.misc.Unsafe;

import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.ByteOrder;
import java.nio.InvalidMarkException;

/**
 * 抽象缓冲器实现，要负责跟踪位置。<br/>
 * 非线程安全，读取字符串的时候使用了共享数组
 */
public abstract class AbstractBuffer implements Buffer {
    protected static final String INITIAL_CAPACITY_OVERFLOW =
            "initial capacity cannot be greater than " + Memory.SIZE_MAX;
    protected static final String MAX_CAPACITY_OVERFLOW = "maximum capacity cannot be greater than " + Memory.SIZE_MAX;
    protected static final Unsafe UNSAFE = Systems.UNSAFE;

    protected Bytes bytes;
    // 缓冲读取的字符串
    protected byte[] chars = new byte[0];
    // 有效位置偏移量(读写的位置都需要大于等于该位置)
    protected long offset;
    // 初始容量
    protected long initialCapacity;
    // 当前容量
    protected long capacity;
    // 最大容量
    protected long maxCapacity;
    // 当前读写位置
    protected long position;
    // 最大读取量
    protected long limit = -1;
    // 标记的位置
    protected long mark = -1;
    // 引用计数器
    protected final Reference reference;
    // 引用对象管理器，可用于对象池
    protected final ReferenceManager<Buffer> referenceManager;
    protected SwappedBuffer swap;

    protected AbstractBuffer(Bytes bytes) {
        this(bytes, 0, bytes.size(), bytes.size(), null, null);
    }

    protected AbstractBuffer(Bytes bytes, ReferenceManager<Buffer> referenceManager) {
        this(bytes, 0, bytes.size(), bytes.size(), null, referenceManager);
    }

    protected AbstractBuffer(Bytes bytes, Reference reference) {
        this(bytes, 0, bytes.size(), bytes.size(), reference, null);
    }

    protected AbstractBuffer(Bytes bytes, Reference reference, ReferenceManager<Buffer> referenceManager) {
        this(bytes, 0, bytes.size(), bytes.size(), reference, referenceManager);
    }

    protected AbstractBuffer(Bytes bytes, long offset, long initialCapacity, long maxCapacity) {
        this(bytes, offset, initialCapacity, maxCapacity, null, null);
    }

    protected AbstractBuffer(Bytes bytes, long offset, long initialCapacity, long maxCapacity, Reference reference,
            ReferenceManager<Buffer> referenceManager) {
        Preconditions.checkNotNull(bytes, "bytes cannot be null");
        Preconditions.checkArgument(offset >= 0, "offset out of bounds of the underlying byte array");
        Preconditions.checkArgument(initialCapacity >= 0, "initialCapacity must be greater than or equals zero");
        Preconditions.checkArgument(maxCapacity >= 0, "maxCapacity must be greater than or equals zero");
        Preconditions.checkArgument(initialCapacity <= maxCapacity,
                "initialCapacity must be smaller than or equals maxCapacity");
        this.reference = reference == null ? new ReferenceCounter() : reference;
        this.bytes = bytes;
        this.offset = offset;
        this.capacity = 0;
        this.initialCapacity = initialCapacity;
        this.maxCapacity = maxCapacity;
        capacity(initialCapacity);
        this.referenceManager = referenceManager;
        this.reference.acquire();
    }

    /**
     * Resets the buffer's internal offset and capacity.
     */
    protected AbstractBuffer reset(final long offset, final long capacity, final long maxCapacity) {
        this.offset = offset;
        this.capacity = 0;
        this.initialCapacity = capacity;
        this.maxCapacity = maxCapacity;
        capacity(initialCapacity);
        rewind();
        return this;
    }

    @Override
    public void acquire() {
        reference.acquire();
    }

    @Override
    public boolean release() {
        if (reference.release()) {
            if (referenceManager != null) {
                referenceManager.release(this);
            } else {
                bytes.close();
            }
            return true;
        }
        return false;
    }

    @Override
    public long references() {
        return reference.references();
    }

    @Override
    public Bytes bytes() {
        return bytes;
    }

    @Override
    public ByteOrder order() {
        return bytes.order();
    }

    @Override
    public Buffer order(final ByteOrder order) {
        Preconditions.checkNotNull(order, "order cannot be null");
        if (order == order()) {
            return this;
        } else if (swap != null) {
            return swap;
        }
        swap = new SwappedBuffer(this, offset, capacity, maxCapacity);
        return swap;
    }

    @Override
    public boolean isDirect() {
        return bytes.isDirect();
    }

    @Override
    public boolean isFile() {
        return bytes.isFile();
    }

    @Override
    public boolean isReadOnly() {
        return false;
    }

    @Override
    public Buffer asReadOnlyBuffer() {
        return new ReadOnlyBuffer(this).reset(offset, capacity, maxCapacity).position(position).limit(limit);
    }

    @Override
    public Buffer compact() {
        compact(offset(position), offset, (limit != -1 ? limit : capacity) - offset(position));
        return clear();
    }

    /**
     * Compacts the given bytes.
     */
    protected void compact(final long from, final long to, final long length) {

    }

    @Override
    public Buffer slice() {
        long maxCapacity = this.maxCapacity - position;
        long capacity = Math.min(Math.min(initialCapacity, maxCapacity), bytes.size() - offset(position));
        if (limit != -1) {
            capacity = maxCapacity = limit - position;
        }
        return new SlicedBuffer(this, offset(position), capacity, maxCapacity);
    }

    @Override
    public Buffer slice(final long length) {
        return new SlicedBuffer(this, checkSlice(position, length), length, length);
    }

    @Override
    public Buffer slice(final long offset, final long length) {
        return new SlicedBuffer(this, checkSlice(offset, length), length, length);
    }

    @Override
    public long offset() {
        return offset;
    }

    @Override
    public long capacity() {
        return capacity;
    }

    /**
     * Updates the buffer capacity.
     */
    public Buffer capacity(final long capacity) {
        Preconditions.checkArgument(capacity <= maxCapacity, "capacity cannot be greater than maximum capacity");
        Preconditions.checkArgument(capacity >= this.capacity, "capacity cannot be decreased");
        if (capacity != this.capacity) {
            // It's possible that the bytes could already meet the requirements of the capacity.
            if (offset(capacity) > bytes.size()) {
                bytes.resize(Memory.Util.toPow2(offset(capacity)));
            }
            this.capacity = capacity;
        }
        return this;
    }

    @Override
    public long maxCapacity() {
        return maxCapacity;
    }

    @Override
    public long position() {
        return position;
    }

    @Override
    public Buffer position(final long position) {
        Preconditions.checkArgument(limit == -1 || position <= limit, "position cannot be greater than limit");
        Preconditions.checkArgument(limit != -1 || position <= maxCapacity, "position cannot be greater than capacity");
        if (position > capacity) {
            capacity(Math.min(maxCapacity, Memory.Util.toPow2(position)));
        }
        this.position = position;
        return this;
    }

    /**
     * 返回绝对位置
     *
     * @param offset 相对位置.
     */
    protected long offset(final long offset) {
        return this.offset + offset;
    }

    @Override
    public long limit() {
        return limit;
    }

    @Override
    public Buffer limit(final long limit) {
        Preconditions.checkArgument(limit <= maxCapacity, "limit cannot be greater than buffer capacity");
        Preconditions.checkArgument(limit >= -1, "limit cannot be negative");
        if (limit != -1 && offset(limit) > bytes.size()) {
            bytes.resize(offset(limit));
        }
        this.limit = limit;
        return this;
    }

    @Override
    public long remaining() {
        return (limit == -1 ? maxCapacity : limit) - position;
    }

    @Override
    public boolean hasRemaining() {
        return remaining() > 0;
    }

    @Override
    public Buffer flip() {
        limit = position;
        position = 0;
        mark = -1;
        return this;
    }

    @Override
    public Buffer mark() {
        this.mark = position;
        return this;
    }

    @Override
    public Buffer rewind() {
        position = 0;
        mark = -1;
        return this;
    }

    @Override
    public Buffer reset() {
        if (mark == -1) {
            throw new InvalidMarkException();
        }
        position = mark;
        return this;
    }

    @Override
    public long skip(final long length) {
        if (length > remaining()) {
            throw new IndexOutOfBoundsException("length cannot be greater than remaining bytes in the buffer");
        }
        position += length;
        return length;
    }

    @Override
    public Buffer clear() {
        position = 0;
        limit = -1;
        mark = -1;
        return this;
    }

    /**
     * 检查边界
     *
     * @param offset 相对位置
     * @param length 长度
     * @return 绝对位置
     */
    protected long checkBoundary(final long offset, final long length, final boolean read) {
        long result = checkOffset(offset);
        if (limit == -1) {
            // 写数据
            if (offset + length > capacity) {
                if (capacity < maxCapacity) {
                    if (result + length <= bytes.size()) {
                        // 缓冲区有数据
                        capacity = Math.min(bytes.size() - this.offset, maxCapacity);
                    } else {
                        // 扩容
                        capacity(calculateCapacity(offset + length));
                    }
                } else if (read) {
                    throw new BufferUnderflowException();
                } else {
                    throw new BufferOverflowException();
                }
            }
        } else if (offset + length > limit) {
            // 读数据
            if (read) {
                throw new BufferUnderflowException();
            } else {
                throw new BufferOverflowException();
            }
        }
        return result;
    }

    /**
     * 检查位置是否越界
     *
     * @param offset 有效数据的相对位置
     * @return 绝对位置
     */
    protected long checkOffset(final long offset) {
        long result = offset(offset);
        if (result < this.offset) {
            throw new IndexOutOfBoundsException();
        } else if (limit == -1) {
            // 写入
            if (offset >= maxCapacity) {
                throw new IndexOutOfBoundsException();
            }
        } else if (offset >= limit) {
            // 读取模式
            throw new IndexOutOfBoundsException();
        }
        return result;
    }

    /**
     * 检查切片是否越界
     *
     * @param offset 偏移量
     * @param length 长度
     * @return 绝对位置
     */
    protected long checkSlice(final long offset, final long length) {
        return checkBoundary(offset, length, true);
    }

    /**
     * 检查指定长度是否越界，并移动读取位置
     *
     * @param length 长度
     * @return 绝对位置
     */
    protected long checkRead(final long length) {
        return checkRead(position, length);
    }

    /**
     * 检查并获取可以读取的数量，在checkOffset之后调用
     *
     * @param offset 偏移位置
     * @param length 最大读取长度
     * @return 可以读取的长度
     * @throws BufferUnderflowException
     */
    protected long availableRead(final long offset, long length) {
        if (limit == -1) {
            // 还在写数据
            return 0;
        }
        return Math.min(length, limit - offset);
    }

    /**
     * 检查读取是否越界
     *
     * @param offset 偏移量
     * @param length 长度
     * @return 绝对位置
     */
    protected long checkRead(final long offset, final long length) {
        return checkBoundary(offset, length, true);
    }


    /**
     * 检查指定长度是否越界
     *
     * @param length 长度
     * @return 绝对位置
     */
    protected long checkWrite(final long length) {
        long result = checkWrite(position, length);
        return result;
    }

    /**
     * 检查写入边界
     *
     * @param offset 偏移量
     * @param length 长度
     * @return 绝对位置
     */
    protected long checkWrite(final long offset, final long length) {
        return checkBoundary(offset, length, false);
    }

    /**
     * Calculates the next capacity that meets the given minimum capacity.
     */
    protected long calculateCapacity(final long minimumCapacity) {
        long newCapacity = Math.min(Math.max(capacity, 2), minimumCapacity);
        while (newCapacity < Math.min(minimumCapacity, maxCapacity)) {
            newCapacity <<= 1;
        }
        return Math.min(newCapacity, maxCapacity);
    }

    @Override
    public ByteArray array(final boolean read) {
        return bytes == null ? null : bytes.array(read);
    }

    @Override
    public Buffer zero() {
        bytes.zero(offset(0));
        return this;
    }

    @Override
    public Buffer zero(final long offset) {
        bytes.zero(checkOffset(offset));
        return this;
    }

    @Override
    public Buffer zero(final long offset, final long length) {
        bytes.zero(checkOffset(offset), length);
        return this;
    }

    @Override
    public long read(final Buffer buffer) {
        Preconditions.checkNotNull(buffer, "buffer cannot be null");
        // 要计算目标绝对位置
        long position = buffer.position();
        long dstOffset = position + buffer.offset();
        long result = read(this.position, buffer.bytes(), dstOffset, buffer.remaining());
        // 写入位置
        buffer.position(position + result);
        return result;
    }

    @Override
    public long read(final Bytes bytes) {
        Preconditions.checkNotNull(bytes, "bytes cannot be null");
        return read(position, bytes, 0, bytes.size());
    }

    @Override
    public long read(final Bytes bytes, final long offset, final long length) {
        return read(position, bytes, offset, length);
    }

    @Override
    public long read(final long srcOffset, final Bytes bytes, final long dstOffset, final long length) {
        long offset = checkOffset(srcOffset);
        long size = availableRead(srcOffset, length);
        if (size <= 0) {
            return 0;
        }
        long result = this.bytes.read(offset, bytes, dstOffset, size);
        position += result;
        return result;
    }

    @Override
    public long read(final byte[] bytes) {
        Preconditions.checkNotNull(bytes, "bytes cannot be null");
        return read(position, bytes, 0, bytes.length);
    }

    @Override
    public long read(final byte[] bytes, final long offset, final long length) {
        return read(position, bytes, offset, length);
    }

    @Override
    public long read(final long srcOffset, final byte[] bytes, final long dstOffset, final long length) {
        long offset = checkOffset(srcOffset);
        long size = availableRead(srcOffset, length);
        if (size <= 0) {
            return 0;
        }
        long result = this.bytes.read(offset, bytes, dstOffset, size);
        position += result;
        return result;
    }

    @Override
    public int readByte() {
        int result = bytes.readByte(checkRead(Bytes.BYTE));
        position += Bytes.BYTE;
        return result;
    }

    @Override
    public int readByte(final long offset) {
        return bytes.readByte(checkRead(offset, Bytes.BYTE));
    }

    @Override
    public int readUnsignedByte() {
        int result = bytes.readUnsignedByte(checkRead(Bytes.BYTE));
        position += Bytes.BYTE;
        return result;
    }

    @Override
    public int readUnsignedByte(final long offset) {
        return bytes.readUnsignedByte(checkRead(offset, Bytes.BYTE));
    }

    @Override
    public char readChar() {
        char result = bytes.readChar(checkRead(Bytes.CHARACTER));
        position += Bytes.CHARACTER;
        return result;
    }

    @Override
    public char readChar(final long offset) {
        return bytes.readChar(checkRead(offset, Bytes.CHARACTER));
    }

    @Override
    public short readShort() {
        short result = bytes.readShort(checkRead(Bytes.SHORT));
        position += Bytes.SHORT;
        return result;
    }

    @Override
    public short readShort(final long offset) {
        return bytes.readShort(checkRead(offset, Bytes.SHORT));
    }

    @Override
    public int readUnsignedShort() {
        int result = bytes.readUnsignedShort(checkRead(Bytes.SHORT));
        position += Bytes.SHORT;
        return result;
    }

    @Override
    public int readUnsignedShort(final long offset) {
        return bytes.readUnsignedShort(checkRead(offset, Bytes.SHORT));
    }

    @Override
    public int readMedium() {
        int result = bytes.readMedium(checkRead(Bytes.MEDIUM));
        position += Bytes.MEDIUM;
        return result;
    }

    @Override
    public int readMedium(final long offset) {
        return bytes.readMedium(checkRead(offset, 3));
    }

    @Override
    public int readUnsignedMedium() {
        int result = bytes.readUnsignedMedium(checkRead(Bytes.MEDIUM));
        position += Bytes.MEDIUM;
        return result;
    }

    @Override
    public int readUnsignedMedium(final long offset) {
        return bytes.readUnsignedMedium(checkRead(offset, 3));
    }

    @Override
    public int readInt() {
        int result = bytes.readInt(checkRead(Bytes.INTEGER));
        position += Bytes.INTEGER;
        return result;
    }

    @Override
    public int readInt(final long offset) {
        return bytes.readInt(checkRead(offset, Bytes.INTEGER));
    }

    @Override
    public long readUnsignedInt() {
        long result = bytes.readUnsignedInt(checkRead(Bytes.INTEGER));
        position += Bytes.INTEGER;
        return result;
    }

    @Override
    public long readUnsignedInt(final long offset) {
        return bytes.readUnsignedInt(checkRead(offset, Bytes.INTEGER));
    }

    @Override
    public long readLong() {
        long result = bytes.readLong(checkRead(Bytes.LONG));
        position += Bytes.LONG;
        return result;
    }

    @Override
    public long readLong(final long offset) {
        return bytes.readLong(checkRead(offset, Bytes.LONG));
    }

    @Override
    public float readFloat() {
        float result = bytes.readFloat(checkRead(Bytes.FLOAT));
        position += Bytes.FLOAT;
        return result;
    }

    @Override
    public float readFloat(final long offset) {
        return bytes.readFloat(checkRead(offset, Bytes.FLOAT));
    }

    @Override
    public double readDouble() {
        double result = bytes.readDouble(checkRead(Bytes.DOUBLE));
        position += Bytes.DOUBLE;
        return result;
    }

    @Override
    public double readDouble(final long offset) {
        return bytes.readDouble(checkRead(offset, Bytes.DOUBLE));
    }

    @Override
    public boolean readBoolean() {
        boolean result = bytes.readBoolean(checkRead(Bytes.BYTE));
        position += Bytes.BYTE;
        return result;
    }

    @Override
    public boolean readBoolean(final long offset) {
        return bytes.readBoolean(checkRead(offset, Bytes.BYTE));
    }

    @Override
    public String readString() {
        int length = readUnsignedShort();
        if (length > chars.length) {
            chars = new byte[length];
        }
        read(chars, 0, length);
        return new String(chars, 0, length);
    }

    @Override
    public String readString(final long offset) {
        int length = readUnsignedShort(offset + Bytes.BYTE);
        if (length > chars.length) {
            chars = new byte[length];
        }
        read(offset + Bytes.BYTE + Bytes.SHORT, chars, 0, length);
        return new String(chars, 0, length);
    }

    @Override
    public String readUTF8() {
        int length = readUnsignedShort();
        if (length > chars.length) {
            chars = new byte[length];
        }
        read(chars, 0, length);
        return new String(chars, 0, length, Charsets.UTF_8);
    }

    @Override
    public String readUTF8(final long offset) {
        int length = readUnsignedShort(offset + Bytes.BYTE);
        if (length > chars.length) {
            chars = new byte[length];
        }
        read(offset + Bytes.BYTE + Bytes.SHORT, chars, 0, length);
        return new String(chars, 0, length, Charsets.UTF_8);
    }

    @Override
    public Buffer write(final Buffer buffer) {
        Preconditions.checkNotNull(buffer, "buffer cannot be null");
        long length = Math.min(buffer.remaining(), remaining());
        write(buffer.bytes(), buffer.offset() + buffer.position(), length);
        buffer.position(buffer.position() + length);
        return this;
    }

    @Override
    public Buffer write(final Bytes bytes) {
        Preconditions.checkNotNull(bytes, "bytes cannot be null");
        this.bytes.write(checkWrite(bytes.size()), bytes, 0, bytes.size());
        position += bytes.size();
        return this;
    }

    @Override
    public Buffer write(final Bytes bytes, final long offset, final long length) {
        this.bytes.write(checkWrite(length), bytes, offset, length);
        position += length;
        return this;
    }

    @Override
    public Buffer write(final long offset, final Bytes bytes, final long srcOffset, final long length) {
        this.bytes.write(checkWrite(offset, length), bytes, srcOffset, length);
        return this;
    }

    @Override
    public Buffer write(final byte[] bytes) {
        Preconditions.checkNotNull(bytes, "bytes cannot be null");
        this.bytes.write(checkWrite(bytes.length), bytes, 0, bytes.length);
        position += bytes.length;
        return this;
    }

    @Override
    public Buffer write(final byte[] bytes, final long offset, final long length) {
        this.bytes.write(checkWrite(length), bytes, offset, length);
        position += length;
        return this;
    }

    @Override
    public Buffer write(final long offset, final byte[] bytes, final long srcOffset, final long length) {
        this.bytes.write(checkWrite(offset, length), bytes, srcOffset, length);
        return this;
    }

    @Override
    public Buffer writeByte(final int b) {
        bytes.writeByte(checkWrite(Bytes.BYTE), b);
        position += Bytes.BYTE;
        return this;
    }

    @Override
    public Buffer writeByte(final long offset, final int b) {
        bytes.writeByte(checkWrite(offset, Bytes.BYTE), b);
        return this;
    }

    @Override
    public Buffer writeUnsignedByte(final int b) {
        bytes.writeUnsignedByte(checkWrite(Bytes.BYTE), b);
        position += Bytes.BYTE;
        return this;
    }

    @Override
    public Buffer writeUnsignedByte(final long offset, final int b) {
        bytes.writeUnsignedByte(checkWrite(offset, Bytes.BYTE), b);
        return this;
    }

    @Override
    public Buffer writeChar(final char c) {
        bytes.writeChar(checkWrite(Bytes.CHARACTER), c);
        position += Bytes.CHARACTER;
        return this;
    }

    @Override
    public Buffer writeChar(final long offset, final char c) {
        bytes.writeChar(checkWrite(offset, Bytes.CHARACTER), c);
        return this;
    }

    @Override
    public Buffer writeShort(final short s) {
        bytes.writeShort(checkWrite(Bytes.SHORT), s);
        position += Bytes.SHORT;
        return this;
    }

    @Override
    public Buffer writeShort(final long offset, final short s) {
        bytes.writeShort(checkWrite(offset, Bytes.SHORT), s);
        return this;
    }

    @Override
    public Buffer writeUnsignedShort(final int s) {
        bytes.writeUnsignedShort(checkWrite(Bytes.SHORT), s);
        position += Bytes.SHORT;
        return this;
    }

    @Override
    public Buffer writeUnsignedShort(final long offset, final int s) {
        bytes.writeUnsignedShort(checkWrite(offset, Bytes.SHORT), s);
        return this;
    }

    @Override
    public Buffer writeMedium(final int m) {
        bytes.writeMedium(checkWrite(Bytes.MEDIUM), m);
        position += Bytes.MEDIUM;
        return this;
    }

    @Override
    public Buffer writeMedium(final long offset, final int m) {
        bytes.writeMedium(checkWrite(offset, 3), m);
        return this;
    }

    @Override
    public Buffer writeUnsignedMedium(final int m) {
        bytes.writeUnsignedMedium(checkWrite(Bytes.MEDIUM), m);
        position += Bytes.MEDIUM;
        return this;
    }

    @Override
    public Buffer writeUnsignedMedium(final long offset, final int m) {
        bytes.writeUnsignedMedium(checkWrite(offset, 3), m);
        return this;
    }

    @Override
    public Buffer writeInt(final int i) {
        bytes.writeInt(checkWrite(Bytes.INTEGER), i);
        position += Bytes.INTEGER;
        return this;
    }

    @Override
    public Buffer writeInt(final long offset, final int i) {
        bytes.writeInt(checkWrite(offset, Bytes.INTEGER), i);
        return this;
    }

    @Override
    public Buffer writeUnsignedInt(final long i) {
        bytes.writeUnsignedInt(checkWrite(Bytes.INTEGER), i);
        position += Bytes.INTEGER;
        return this;
    }

    @Override
    public Buffer writeUnsignedInt(final long offset, final long i) {
        bytes.writeUnsignedInt(checkWrite(offset, Bytes.INTEGER), i);
        return this;
    }

    @Override
    public Buffer writeLong(final long l) {
        bytes.writeLong(checkWrite(Bytes.LONG), l);
        position += Bytes.LONG;
        return this;
    }

    @Override
    public Buffer writeLong(final long offset, final long l) {
        bytes.writeLong(checkWrite(offset, Bytes.LONG), l);
        return this;
    }

    @Override
    public Buffer writeFloat(final float f) {
        bytes.writeFloat(checkWrite(Bytes.FLOAT), f);
        position += Bytes.FLOAT;
        return this;
    }

    @Override
    public Buffer writeFloat(final long offset, final float f) {
        bytes.writeFloat(checkWrite(offset, Bytes.FLOAT), f);
        return this;
    }

    @Override
    public Buffer writeDouble(final double d) {
        bytes.writeDouble(checkWrite(Bytes.DOUBLE), d);
        position += Bytes.DOUBLE;
        return this;
    }

    @Override
    public Buffer writeDouble(final long offset, final double d) {
        bytes.writeDouble(checkWrite(offset, Bytes.DOUBLE), d);
        return this;
    }

    @Override
    public Buffer writeBoolean(final boolean b) {
        bytes.writeBoolean(checkWrite(Bytes.BYTE), b);
        position += Bytes.BYTE;
        return this;
    }

    @Override
    public Buffer writeBoolean(final long offset, final boolean b) {
        bytes.writeBoolean(checkWrite(offset, Bytes.BYTE), b);
        return this;
    }

    @Override
    public Buffer writeString(final String s) {
        byte[] bytes = s == null ? new byte[0] : s.getBytes();
        return writeUnsignedShort(bytes.length).write(bytes, 0, bytes.length);
    }

    @Override
    public Buffer writeString(final long offset, final String s) {
        byte[] bytes = s == null ? new byte[0] : s.getBytes();
        return writeUnsignedShort(offset + Bytes.BYTE, bytes.length)
                .write(offset + Bytes.MEDIUM, bytes, 0, bytes.length);
    }

    @Override
    public Buffer writeUTF8(final String s) {
        byte[] bytes = s == null ? new byte[0] : s.getBytes(Charsets.UTF_8);
        return writeUnsignedShort(bytes.length).write(bytes, 0, bytes.length);
    }

    @Override
    public Buffer writeUTF8(final long offset, final String s) {
        byte[] bytes = s == null ? new byte[0] : s.getBytes(Charsets.UTF_8);
        return writeUnsignedShort(offset + Bytes.BYTE, bytes.length)
                .write(offset + Bytes.MEDIUM, bytes, 0, bytes.length);
    }

    @Override
    public Buffer flush() {
        bytes.flush();
        return this;
    }

    @Override
    public void close() {
        release();
    }

}
