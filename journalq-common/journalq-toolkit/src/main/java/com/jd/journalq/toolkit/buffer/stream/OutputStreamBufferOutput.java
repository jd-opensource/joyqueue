package com.jd.journalq.toolkit.buffer.stream;

import com.jd.journalq.toolkit.buffer.Buffer;
import com.jd.journalq.toolkit.buffer.BufferOutput;
import com.jd.journalq.toolkit.buffer.ByteArray;
import com.jd.journalq.toolkit.buffer.IOWrapException;
import com.jd.journalq.toolkit.buffer.bytes.Bytes;
import com.jd.journalq.toolkit.lang.Preconditions;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Output stream output.
 */
public class OutputStreamBufferOutput implements BufferOutput<BufferOutput<?>> {
    protected final DataOutputStream os;

    public OutputStreamBufferOutput(final OutputStream os) {
        this(new DataOutputStream(os));
    }

    public OutputStreamBufferOutput(final DataOutputStream os) {
        Preconditions.checkNotNull(os, "output stream cannot be null");
        this.os = os;
    }

    @Override
    public BufferOutput<?> write(final Buffer buffer) {
        // TODO 要测试
        Preconditions.checkNotNull(buffer, "buffer cannot be null");
        ByteArray array = buffer.array(true);
        try {
            long start = buffer.position();
            int remaining = (int) buffer.remaining();
            if (array != null) {
                // 要转换成数组的绝对位置
                os.write(array.array(), (int) (array.offset() + buffer.offset() + start), remaining);
            } else {
                // 4K数组，如果都加载，可能内存不够
                int size = Math.min(remaining, Bytes.SIZE_4K);
                byte[] data = new byte[size];
                while (remaining > 0) {
                    buffer.read(start, data, 0, size);
                    os.write(data, 0, size);
                    remaining -= size;
                    start += size;
                    size = Math.min(remaining, size);
                }
            }
        } catch (IOException e) {
            throw new IOWrapException(e);
        }
        return this;
    }

    @Override
    public BufferOutput<?> write(final Bytes bytes) {
        Preconditions.checkNotNull(bytes, "bytes cannot be null");
        // TODO 要测试
        ByteArray array = bytes.array(true);
        try {
            if (array != null) {
                os.write(array.array(), array.offset(), array.length());
            } else {
                // 4K数组，如果都加载，可能内存不够
                int remaining = (int) bytes.size();
                int size = Math.min(remaining, Bytes.SIZE_4K);
                byte[] data = new byte[size];
                int start = 0;
                while (remaining > 0) {
                    bytes.read(start, data, 0, size);
                    os.write(data, 0, size);
                    remaining -= size;
                    start += size;
                    size = Math.min(remaining, size);
                }
            }
        } catch (IOException e) {
            throw new IOWrapException(e);
        }
        return this;
    }

    @Override
    public BufferOutput<?> write(final byte[] bytes) {
        Preconditions.checkNotNull(bytes, "bytes cannot be null");
        try {
            os.write(bytes);
        } catch (IOException e) {
            throw new IOWrapException(e);
        }
        return this;
    }

    @Override
    public BufferOutput<?> write(final Bytes bytes, final long offset, final long length) {
        Preconditions.checkNotNull(bytes, "bytes cannot be null");
        ByteArray array = bytes.array(true);
        // TODO 要测试
        try {
            if (array != null) {
                os.write(array.array(), (int) (offset + array.offset()), (int) length);
            } else {
                // 4K数组，如果都加载，可能内存不够
                int remaining = (int) length;
                int size = Math.min(remaining, Bytes.SIZE_4K);
                byte[] data = new byte[size];
                int start = 0;
                while (remaining > 0) {
                    bytes.read(start, data, 0, size);
                    os.write(data, 0, size);
                    remaining -= size;
                    start += size;
                    size = Math.min(remaining, size);
                }
            }
        } catch (IOException e) {
            throw new IOWrapException(e);
        }
        return this;
    }

    @Override
    public BufferOutput<?> write(final byte[] bytes, final long offset, final long length) {
        try {
            os.write(bytes, (int) offset, (int) length);
        } catch (IOException e) {
            throw new IOWrapException(e);
        }
        return this;
    }

    @Override
    public BufferOutput<?> writeByte(final int b) {
        try {
            os.writeByte(b);
        } catch (IOException e) {
            throw new IOWrapException(e);
        }
        return this;
    }

    @Override
    public BufferOutput<?> writeUnsignedByte(final int b) {
        try {
            os.writeByte(b);
        } catch (IOException e) {
            throw new IOWrapException(e);
        }
        return this;
    }

    @Override
    public BufferOutput<?> writeChar(final char c) {
        try {
            os.writeChar(c);
        } catch (IOException e) {
            throw new IOWrapException(e);
        }
        return this;
    }

    @Override
    public BufferOutput<?> writeShort(final short s) {
        try {
            os.writeShort(s);
        } catch (IOException e) {
            throw new IOWrapException(e);
        }
        return this;
    }

    @Override
    public BufferOutput<?> writeUnsignedShort(final int s) {
        try {
            os.writeShort(s);
        } catch (IOException e) {
            throw new IOWrapException(e);
        }
        return this;
    }

    @Override
    public BufferOutput<?> writeMedium(final int m) {
        try {
            os.writeByte((byte) (m >>> 16));
            os.writeByte((byte) (m >>> 8));
            os.writeByte((byte) m);
        } catch (IOException e) {
            throw new IOWrapException(e);
        }
        return this;
    }

    @Override
    public BufferOutput<?> writeUnsignedMedium(final int m) {
        return writeMedium(m);
    }

    @Override
    public BufferOutput<?> writeInt(final int i) {
        try {
            os.writeInt(i);
        } catch (IOException e) {
            throw new IOWrapException(e);
        }
        return this;
    }

    @Override
    public BufferOutput<?> writeUnsignedInt(final long i) {
        try {
            os.writeInt((int) i);
        } catch (IOException e) {
            throw new IOWrapException(e);
        }
        return this;
    }

    @Override
    public BufferOutput<?> writeLong(final long l) {
        try {
            os.writeLong(l);
        } catch (IOException e) {
            throw new IOWrapException(e);
        }
        return this;
    }

    @Override
    public BufferOutput<?> writeFloat(final float f) {
        try {
            os.writeFloat(f);
        } catch (IOException e) {
            throw new IOWrapException(e);
        }
        return this;
    }

    @Override
    public BufferOutput<?> writeDouble(final double d) {
        try {
            os.writeDouble(d);
        } catch (IOException e) {
            throw new IOWrapException(e);
        }
        return this;
    }

    @Override
    public BufferOutput<?> writeBoolean(final boolean b) {
        try {
            os.writeBoolean(b);
        } catch (IOException e) {
            throw new IOWrapException(e);
        }
        return this;
    }

    @Override
    public BufferOutput<?> writeString(final String s) {
        try {
            os.writeUTF(s);
        } catch (IOException e) {
            throw new IOWrapException(e);
        }
        return this;
    }

    @Override
    public BufferOutput<?> writeUTF8(final String s) {
        try {
            os.writeUTF(s);
        } catch (IOException e) {
            throw new IOWrapException(e);
        }
        return this;
    }

    @Override
    public BufferOutput<?> flush() {
        try {
            os.flush();
        } catch (IOException e) {
            throw new IOWrapException(e);
        }
        return this;
    }

    @Override
    public void close() {
        try {
            os.close();
        } catch (IOException e) {
            throw new IOWrapException(e);
        }
    }

}
