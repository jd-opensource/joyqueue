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
import com.jd.journalq.toolkit.lang.Preconditions;

import java.nio.ByteOrder;

/**
 * Wrapped bytes.
 */
public class WrappedBytes extends AbstractBytes {
    protected final Bytes bytes;
    protected final Bytes root;

    public WrappedBytes(final Bytes bytes) {
        Preconditions.checkNotNull(bytes, "bytes cannot be null");
        this.bytes = bytes;
        if (bytes instanceof WrappedBytes) {
            Bytes parent = bytes;
            while (parent != null && parent instanceof WrappedBytes) {
                parent = ((WrappedBytes) parent).root();
            }
            this.root = parent;
        } else {
            this.root = bytes;
        }
    }

    /**
     * Returns the root bytes.
     */
    public Bytes root() {
        return root;
    }

    @Override
    public ByteArray array(final boolean read) {
        return root.array(read);
    }

    @Override
    public long size() {
        return bytes.size();
    }

    @Override
    public Bytes resize(long newSize) {
        return bytes.resize(newSize);
    }

    @Override
    public ByteOrder order() {
        return bytes.order();
    }

    @Override
    public Bytes zero() {
        bytes.zero();
        return this;
    }

    @Override
    public Bytes zero(final long offset) {
        bytes.zero(offset);
        return this;
    }

    @Override
    public Bytes zero(final long offset, final long length) {
        bytes.zero(offset, length);
        return this;
    }

    @Override
    public long read(final long offset, final Bytes dst, final long dstOffset, final long length) {
        return bytes.read(offset, dst, dstOffset, length);
    }

    @Override
    public long read(final long offset, final byte[] dst, final long dstOffset, final long length) {
        return bytes.read(offset, dst, dstOffset, length);
    }

    @Override
    public int readByte(final long offset) {
        return bytes.readByte(offset);
    }

    @Override
    public int readUnsignedByte(final long offset) {
        return bytes.readUnsignedByte(offset);
    }

    @Override
    public char readChar(final long offset) {
        return bytes.readChar(offset);
    }

    @Override
    public short readShort(final long offset) {
        return bytes.readShort(offset);
    }

    @Override
    public int readUnsignedShort(final long offset) {
        return bytes.readUnsignedShort(offset);
    }

    @Override
    public int readMedium(final long offset) {
        return bytes.readMedium(offset);
    }

    @Override
    public int readUnsignedMedium(final long offset) {
        return bytes.readUnsignedMedium(offset);
    }

    @Override
    public int readInt(final long offset) {
        return bytes.readInt(offset);
    }

    @Override
    public long readUnsignedInt(final long offset) {
        return bytes.readUnsignedInt(offset);
    }

    @Override
    public long readLong(final long offset) {
        return bytes.readLong(offset);
    }

    @Override
    public float readFloat(final long offset) {
        return bytes.readFloat(offset);
    }

    @Override
    public double readDouble(final long offset) {
        return bytes.readDouble(offset);
    }

    @Override
    public boolean readBoolean(final long offset) {
        return bytes.readBoolean(offset);
    }

    @Override
    public String readString(final long offset) {
        return bytes.readString(offset);
    }

    @Override
    public String readUTF8(final long offset) {
        return bytes.readUTF8(offset);
    }

    @Override
    public Bytes write(final long offset, final Bytes src, final long srcOffset, final long length) {
        bytes.write(offset, src, srcOffset, length);
        return this;
    }

    @Override
    public Bytes write(final long offset, final byte[] src, final long srcOffset, final long length) {
        bytes.write(offset, src, srcOffset, length);
        return this;
    }

    @Override
    public Bytes writeByte(final long offset, final int b) {
        bytes.writeByte(offset, b);
        return this;
    }

    @Override
    public Bytes writeUnsignedByte(final long offset, final int b) {
        bytes.writeUnsignedByte(offset, b);
        return this;
    }

    @Override
    public Bytes writeChar(final long offset, final char c) {
        bytes.writeChar(offset, c);
        return this;
    }

    @Override
    public Bytes writeShort(final long offset, final short s) {
        bytes.writeShort(offset, s);
        return this;
    }

    @Override
    public Bytes writeUnsignedShort(final long offset, final int s) {
        bytes.writeUnsignedShort(offset, s);
        return this;
    }

    @Override
    public Bytes writeMedium(final long offset, final int m) {
        bytes.writeMedium(offset, m);
        return this;
    }

    @Override
    public Bytes writeUnsignedMedium(final long offset, final int m) {
        bytes.writeUnsignedMedium(offset, m);
        return this;
    }

    @Override
    public Bytes writeInt(final long offset, final int i) {
        bytes.writeInt(offset, i);
        return this;
    }

    @Override
    public Bytes writeUnsignedInt(final long offset, final long i) {
        bytes.writeUnsignedInt(offset, i);
        return this;
    }

    @Override
    public Bytes writeLong(final long offset, final long l) {
        bytes.writeLong(offset, l);
        return this;
    }

    @Override
    public Bytes writeFloat(final long offset, final float f) {
        bytes.writeFloat(offset, f);
        return this;
    }

    @Override
    public Bytes writeDouble(final long offset, final double d) {
        bytes.writeDouble(offset, d);
        return this;
    }

    @Override
    public Bytes writeBoolean(final long offset, final boolean b) {
        bytes.writeBoolean(offset, b);
        return this;
    }

    @Override
    public Bytes writeString(final long offset, final String s) {
        bytes.writeString(offset, s);
        return this;
    }

    @Override
    public Bytes writeUTF8(final long offset, final String s) {
        bytes.writeUTF8(offset, s);
        return this;
    }

    @Override
    public Bytes flush() {
        bytes.flush();
        return this;
    }

    @Override
    public void close() {
        bytes.close();
    }

}
