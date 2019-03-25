package com.jd.journalq.toolkit.buffer.stream;

import com.jd.journalq.toolkit.buffer.Buffer;
import com.jd.journalq.toolkit.buffer.BufferInput;
import com.jd.journalq.toolkit.buffer.ByteArray;
import com.jd.journalq.toolkit.buffer.IOWrapException;
import com.jd.journalq.toolkit.buffer.bytes.Bytes;
import com.jd.journalq.toolkit.lang.Preconditions;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Input stream buffer input.
 */
public class InputStreamBufferInput implements BufferInput {
    private final DataInputStream is;

    public InputStreamBufferInput(InputStream is) {
        this(new DataInputStream(is));
    }

    public InputStreamBufferInput(DataInputStream is) {
        Preconditions.checkNotNull(is, "input stream cannot be null");
        this.is = is;
    }

    @Override
    public long remaining() {
        try {
            return is.available();
        } catch (IOException e) {
            throw new IOWrapException(e);
        }
    }

    @Override
    public boolean hasRemaining() {
        return remaining() > 0;
    }

    @Override
    public long skip(final long bytes) {
        try {
            return is.skip(bytes);
        } catch (IOException e) {
            throw new IOWrapException(e);
        }
    }

    @Override
    public long read(final Bytes bytes) {
        Preconditions.checkNotNull(bytes, "bytes cannot be null");
        // TODO 测试
        ByteArray array = bytes.array(false);
        try {
            int count;
            int pos = 0;
            long total = 0;
            int remaining = Math.min((int) bytes.size(), (int) remaining());
            if (array != null) {
                // 数组，尽可能的拷贝数据
                total = is.read(array.array(), array.offset(), remaining);
            } else {
                // 4K数组，如果都加载，可能内存不够
                byte[] data = new byte[Bytes.SIZE_4K];
                while (remaining > 0) {
                    count = is.read(data, 0, data.length);
                    if (count == -1) {
                        break;
                    }
                    bytes.write(pos, data, 0, count);
                    remaining -= count;
                    pos += count;
                    total += count;
                }
            }
            return total;
        } catch (IOException e) {
            throw new IOWrapException(e);
        }
    }

    @Override
    public long read(final Buffer buffer) {
        Preconditions.checkNotNull(buffer, "buffer cannot be null");
        // TODO 需要测试
        ByteArray array = buffer.array(false);
        try {
            int count;
            long total = 0;
            int position = (int) buffer.position();
            int remaining = Math.min((int) buffer.remaining(), (int) remaining());
            if (array != null) {
                // 数组，尽可能的拷贝数据
                total = is.read(array.array(), (int) (array.offset() + buffer.offset() + position), remaining);
                position += total;
            } else {
                // 4K数组，如果都加载，可能内存不够
                byte[] data = new byte[Bytes.SIZE_4K];
                while (remaining > 0) {
                    count = is.read(data, 0, data.length);
                    if (count == -1) {
                        break;
                    }
                    buffer.write(data, 0, count);
                    remaining -= count;
                    total += count;
                }
            }
            buffer.position(position + total);
            return total;
        } catch (IOException e) {
            throw new IOWrapException(e);
        }
    }

    @Override
    public long read(final byte[] bytes) {
        try {
            return is.read(bytes);
        } catch (IOException e) {
            throw new IOWrapException(e);
        }
    }

    @Override
    public long read(final Bytes bytes, final long offset, final long length) {
        Preconditions.checkNotNull(bytes, "bytes cannot be null");
        Preconditions.checkArgument(length >= 0, "length is invalid");
        Preconditions.checkElementIndex((int) offset, (int) bytes.size(), "offset is invalid");
        // TODO 测试
        ByteArray array = bytes.array(false);
        try {
            int count;
            long total = 0;
            int remaining = Math.min(Math.min((int) length, (int) remaining()), (int) (bytes.size() - offset));
            if (array != null) {
                total = is.read(array.array(), (int) (array.offset() + offset), remaining);
            } else {
                // 4K数组，如果都加载，可能内存不够
                byte[] data = new byte[Bytes.SIZE_4K];
                int pos = (int) offset;
                while (remaining > 0) {
                    count = is.read(data, 0, data.length);
                    if (count == -1) {
                        break;
                    }
                    bytes.write(pos, data, 0, count);
                    remaining -= count;
                    pos += count;
                }
            }
            return total;
        } catch (IOException e) {
            throw new IOWrapException(e);
        }
    }

    @Override
    public long read(final byte[] bytes, final long offset, final long length) {
        try {
            return is.read(bytes, (int) offset, (int) length);
        } catch (IOException e) {
            throw new IOWrapException(e);
        }
    }

    @Override
    public int readByte() {
        try {
            return is.readByte();
        } catch (IOException e) {
            throw new IOWrapException(e);
        }
    }

    @Override
    public int readUnsignedByte() {
        try {
            return is.readUnsignedByte();
        } catch (IOException e) {
            throw new IOWrapException(e);
        }
    }

    @Override
    public char readChar() {
        try {
            return is.readChar();
        } catch (IOException e) {
            throw new IOWrapException(e);
        }
    }

    @Override
    public short readShort() {
        try {
            return is.readShort();
        } catch (IOException e) {
            throw new IOWrapException(e);
        }
    }

    @Override
    public int readUnsignedShort() {
        try {
            return is.readUnsignedShort();
        } catch (IOException e) {
            throw new IOWrapException(e);
        }
    }

    @Override
    public int readMedium() {
        try {
            return is.readByte() << 16 | (is.readByte() & 0xff) << 8 | (is.readByte() & 0xff);
        } catch (IOException e) {
            throw new IOWrapException(e);
        }
    }

    @Override
    public int readUnsignedMedium() {
        try {
            return (is.readByte() & 0xff) << 16 | (is.readByte() & 0xff) << 8 | (is.readByte() & 0xff);
        } catch (IOException e) {
            throw new IOWrapException(e);
        }
    }

    @Override
    public int readInt() {
        try {
            return is.readInt();
        } catch (IOException e) {
            throw new IOWrapException(e);
        }
    }

    @Override
    public long readUnsignedInt() {
        try {
            return is.readInt() & 0xFFFFFFFFL;
        } catch (IOException e) {
            throw new IOWrapException(e);
        }
    }

    @Override
    public long readLong() {
        try {
            return is.readLong();
        } catch (IOException e) {
            throw new IOWrapException(e);
        }
    }

    @Override
    public float readFloat() {
        try {
            return is.readFloat();
        } catch (IOException e) {
            throw new IOWrapException(e);
        }
    }

    @Override
    public double readDouble() {
        try {
            return is.readDouble();
        } catch (IOException e) {
            throw new IOWrapException(e);
        }
    }

    @Override
    public boolean readBoolean() {
        try {
            return is.readBoolean();
        } catch (IOException e) {
            throw new IOWrapException(e);
        }
    }

    @Override
    public String readString() {
        try {
            return is.readUTF();
        } catch (IOException e) {
            throw new IOWrapException(e);
        }
    }

    @Override
    public String readUTF8() {
        try {
            return is.readUTF();
        } catch (IOException e) {
            throw new IOWrapException(e);
        }
    }

    @Override
    public void close() {
        try {
            is.close();
        } catch (IOException e) {
            throw new IOWrapException(e);
        }
    }

}
