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

import com.jd.journalq.toolkit.buffer.stream.BufferOutputStream;
import com.jd.journalq.toolkit.lang.Preconditions;

import java.io.DataOutput;

/**
 * Buffer data output wrapper.
 */
public class BufferDataOutput extends BufferOutputStream implements DataOutput {
    protected final BufferOutput<?> buffer;

    public BufferDataOutput(BufferOutput<?> buffer) {
        super(buffer);
        Preconditions.checkNotNull(buffer, "buffer cannot be null");
        this.buffer = buffer;
    }

    @Override
    public void write(final int b) {
        buffer.writeByte(b);
    }

    @Override
    public void write(final byte[] b) {
        buffer.write(b);
    }

    @Override
    public void write(final byte[] b, final int off, final int len) {
        buffer.write(b, off, len);
    }

    @Override
    public void writeBoolean(final boolean b) {
        buffer.writeBoolean(b);
    }

    @Override
    public void writeByte(final int b) {
        buffer.writeByte(b);
    }

    @Override
    public void writeShort(final int s) {
        buffer.writeShort((short) s);
    }

    @Override
    public void writeChar(final int c) {
        buffer.writeChar((char) c);
    }

    @Override
    public void writeInt(final int i) {
        buffer.writeInt(i);
    }

    @Override
    public void writeLong(final long l) {
        buffer.writeLong(l);
    }

    @Override
    public void writeFloat(final float f) {
        buffer.writeFloat(f);
    }

    @Override
    public void writeDouble(final double d) {
        buffer.writeDouble(d);
    }

    @Override
    public void writeBytes(final String s) {
        if (s != null) {
            buffer.write(s.getBytes());
        }
    }

    @Override
    public void writeChars(final String s) {
        if (s != null) {
            for (char c : s.toCharArray()) {
                buffer.writeChar(c);
            }
        }
    }

    @Override
    public void writeUTF(final String s) {
        buffer.writeUTF8(s);
    }

}
