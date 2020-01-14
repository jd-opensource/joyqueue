/**
 * Copyright 2019 The JoyQueue Authors.
 *
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
package org.joyqueue.toolkit.buffer;

import org.joyqueue.toolkit.ref.Reference;
import org.joyqueue.toolkit.ref.ReferenceCounter;

import java.io.Closeable;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 带引用计数器的字节缓冲区
 */
public class RByteBuffer implements Closeable {
    // 缓冲区
    protected ByteBuffer buffer;
    // 引用资源
    protected Reference reference;
    protected AtomicBoolean released = new AtomicBoolean(false);

    public RByteBuffer(ByteBuffer buffer, Reference reference) {
        if (buffer == null) {
            throw new IllegalArgumentException("buffer can not be null");
        }
        this.buffer = buffer;
        this.reference = reference == null ? new ReferenceCounter() : reference;
        this.reference.acquire();
    }

    public ByteBuffer getBuffer() {
        return buffer;
    }

    public Reference getReference() {
        return reference;
    }

    public final int capacity() {
        return buffer.capacity();
    }

    public final int position() {
        return buffer.position();
    }

    public final Buffer position(final int newPosition) {
        return buffer.position(newPosition);
    }

    public final int limit() {
        return buffer.limit();
    }

    public final Buffer limit(final int newLimit) {
        return buffer.limit(newLimit);
    }

    public final Buffer mark() {
        return buffer.mark();
    }

    public final Buffer reset() {
        return buffer.reset();
    }

    public final Buffer clear() {
        return buffer.clear();
    }

    public final Buffer flip() {
        return buffer.flip();
    }

    public final Buffer rewind() {
        return buffer.rewind();
    }

    public final int remaining() {
        return buffer.remaining();
    }

    public final boolean hasRemaining() {
        return buffer.hasRemaining();
    }

    public final boolean isReadOnly() {
        return buffer.isReadOnly();
    }

    public final boolean hasArray() {
        return buffer.hasArray();
    }

    public final int arrayOffset() {
        return buffer.arrayOffset();
    }

    public final boolean isDirect() {
        return buffer.isDirect();
    }

    public final ByteBuffer slice() {
        return buffer.slice();
    }

    public final byte get() {
        return buffer.get();
    }

    public final ByteBuffer put(final byte b) {
        return buffer.put(b);
    }

    public final byte get(final int index) {
        return buffer.get(index);
    }

    public final ByteBuffer put(final int index, byte b) {
        return buffer.put(index, b);
    }

    public final ByteBuffer get(final byte[] dst, final int offset, final int length) {
        return buffer.get(dst, offset, length);
    }

    public final ByteBuffer get(final byte[] dst) {
        return buffer.get(dst);
    }

    public final ByteBuffer put(final ByteBuffer src) {
        return buffer.put(src);
    }

    public final ByteBuffer put(final byte[] src, final int offset, final int length) {
        return buffer.put(src, offset, length);
    }

    public final ByteBuffer put(final byte[] src) {
        return buffer.put(src, 0, src.length);
    }

    public final byte[] array() {
        return buffer.array();
    }

    public final ByteBuffer compact() {
        return buffer.compact();
    }

    public final char getChar() {
        return buffer.getChar();
    }

    public final ByteBuffer putChar(final char value) {
        return buffer.putChar(value);
    }

    public final char getChar(final int index) {
        return buffer.getChar(index);
    }

    public final ByteBuffer putChar(final int index, final char value) {
        return buffer.putChar(index, value);
    }

    public final short getShort() {
        return buffer.getShort();
    }

    public final ByteBuffer putShort(final short value) {
        return buffer.putShort(value);
    }

    public final short getShort(final int index) {
        return buffer.getShort(index);
    }

    public final ByteBuffer putShort(final int index, final short value) {
        return buffer.putShort(index, value);
    }

    public final int getInt() {
        return buffer.getInt();
    }

    public final ByteBuffer putInt(final int value) {
        return buffer.putInt(value);
    }

    public final int getInt(final int index) {
        return buffer.getInt(index);
    }

    public final ByteBuffer putInt(final int index, final int value) {
        return buffer.putInt(index, value);
    }

    public final long getLong() {
        return buffer.getLong();
    }

    public final ByteBuffer putLong(final long value) {
        return buffer.putLong(value);
    }

    public final long getLong(final int index) {
        return buffer.getLong(index);
    }

    public final ByteBuffer putLong(final int index, final long value) {
        return buffer.putLong(index, value);
    }

    public final float getFloat() {
        return buffer.getFloat();
    }

    public final ByteBuffer putFloat(final float value) {
        return buffer.putFloat(value);
    }

    public final float getFloat(final int index) {
        return buffer.getFloat(index);
    }

    public final ByteBuffer putFloat(final int index, final float value) {
        return buffer.putFloat(index, value);
    }

    public final double getDouble() {
        return buffer.getDouble();
    }

    public final ByteBuffer putDouble(final double value) {
        return buffer.putDouble(value);
    }

    public final double getDouble(final int index) {
        return buffer.getDouble(index);
    }

    public final ByteBuffer putDouble(final int index, final double value) {
        return buffer.putDouble(index, value);
    }

    public final boolean release() {
        if (released.compareAndSet(false, true)) {
            return reference.release();
        }
        return false;
    }

    @Override
    public void close() {
        release();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        RByteBuffer that = (RByteBuffer) o;

        if (buffer != null ? !buffer.equals(that.buffer) : that.buffer != null) {
            return false;
        }
        return reference != null ? reference.equals(that.reference) : that.reference == null;

    }

    @Override
    public int hashCode() {
        int result = buffer != null ? buffer.hashCode() : 0;
        result = 31 * result + (reference != null ? reference.hashCode() : 0);
        return result;
    }
}
