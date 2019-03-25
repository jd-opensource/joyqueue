package com.jd.journalq.toolkit.buffer.bytes;


import com.jd.journalq.toolkit.buffer.ByteArray;
import com.jd.journalq.toolkit.buffer.IOWrapException;
import com.jd.journalq.toolkit.buffer.memory.MappedMemoryAllocator;
import com.jd.journalq.toolkit.buffer.memory.Memory;
import com.jd.journalq.toolkit.lang.Preconditions;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;

/**
 * File bytes.
 * <p>
 * File bytes wrap a simple {@link java.io.RandomAccessFile} instance to provide random access to a randomAccessFile
 * on local disk. All
 * operations are delegated directly to the {@link java.io.RandomAccessFile} interface, and limitations are dependent
 * on the
 * semantics of the underlying randomAccessFile.
 * <p>
 * Bytes are always stored in the underlying randomAccessFile in {@link java.nio.ByteOrder#BIG_ENDIAN} order.
 * To flip the byte order to read or write to/from a randomAccessFile in {@link java.nio.ByteOrder#LITTLE_ENDIAN}
 * order use
 * {@link Bytes#order(java.nio.ByteOrder)}.
 */
public class FileBytes extends AbstractBytes {
    public static final String DEFAULT_MODE = "rw";

    /**
     * Allocates a randomAccessFile buffer of unlimited count.
     * <p>
     * The buffer will be allocated with {@link Long#MAX_VALUE} bytes. As bytes are written to the buffer, the
     * underlying
     * {@link java.io.RandomAccessFile} will expand.
     *
     * @param file The randomAccessFile to allocate.
     * @return The allocated buffer.
     */
    public static FileBytes allocate(final File file) {
        return allocate(file, DEFAULT_MODE, Long.MAX_VALUE);
    }

    /**
     * Allocates a randomAccessFile buffer.
     * <p>
     * If the underlying randomAccessFile is empty, the randomAccessFile count will expand dynamically as bytes are
     * written to the randomAccessFile.
     *
     * @param file The randomAccessFile to allocate.
     * @param size The count of the bytes to allocate.
     * @return The allocated buffer.
     */
    public static FileBytes allocate(final File file, final long size) {
        return allocate(file, DEFAULT_MODE, size);
    }

    /**
     * Allocates a randomAccessFile buffer.
     * <p>
     * If the underlying randomAccessFile is empty, the randomAccessFile count will expand dynamically as bytes are
     * written to the randomAccessFile.
     *
     * @param file The randomAccessFile to allocate.
     * @param mode The mode in which to open the underlying {@link java.io.RandomAccessFile}.
     * @param size The count of the bytes to allocate.
     * @return The allocated buffer.
     */
    public static FileBytes allocate(final File file, final String mode, final long size) {
        return new FileBytes(file, mode, Memory.Util.toPow2(size));
    }

    private File file;
    private RandomAccessFile randomAccessFile;
    private long size;

    public FileBytes(final File file, final String mode, final long size) {
        Preconditions.checkNotNull(file, "file cannot be null");
        Preconditions.checkArgument(size >= 0, "size must be positive");
        this.file = file;
        this.size = size;
        try {
            this.randomAccessFile = new RandomAccessFile(file, mode == null ? DEFAULT_MODE : mode);
            if (size > randomAccessFile.length()) {
                randomAccessFile.setLength(size);
            }
        } catch (IOException e) {
            throw new IOWrapException(e);
        }
    }

    /**
     * Returns the underlying file object.
     *
     * @return The underlying file.
     */
    public File file() {
        return file;
    }

    @Override
    public long size() {
        return size;
    }

    @Override
    public Bytes resize(final long newSize) {
        Preconditions
                .checkArgument(newSize >= size, "cannot decrease file bytes size; use zero() to decrease file size");
        try {
            long length = randomAccessFile.length();
            if (newSize > length) {
                randomAccessFile.setLength(newSize);
            }
            this.size = newSize;
        } catch (IOException e) {
            throw new IOWrapException(e);
        }
        return this;
    }

    @Override
    public boolean isFile() {
        return true;
    }

    /**
     * Maps a portion of the randomAccessFile into memory in {@link java.nio.channels.FileChannel.MapMode#READ_WRITE}
     * mode and returns
     * a {@link MappedBytes} instance.
     *
     * @param offset The offset from which to map the randomAccessFile into memory.
     * @param size   The count of the bytes to map into memory.
     * @return The mapped bytes.
     * @throws IllegalArgumentException If {@code count} is greater than the maximum allowed
     *                                  {@link java.nio.MappedByteBuffer} count: {@link Integer#MAX_VALUE}
     */
    public MappedBytes map(final long offset, final long size) {
        return map(offset, size, FileChannel.MapMode.READ_WRITE);
    }

    /**
     * Maps a portion of the randomAccessFile into memory and returns a {@link MappedBytes} instance.
     *
     * @param offset The offset from which to map the randomAccessFile into memory.
     * @param size   The count of the bytes to map into memory.
     * @param mode   The mode in which to map the randomAccessFile into memory.
     * @return The mapped bytes.
     * @throws IllegalArgumentException If {@code count} is greater than the maximum allowed
     *                                  {@link java.nio.MappedByteBuffer} count: {@link Integer#MAX_VALUE}
     */
    public MappedBytes map(final long offset, final long size, final FileChannel.MapMode mode) {
        return new MappedBytes(file, new MappedMemoryAllocator(randomAccessFile, mode, offset).allocate(size));
    }

    @Override
    public ByteOrder order() {
        return ByteOrder.BIG_ENDIAN;
    }

    /**
     * Seeks to the given offset.
     */
    protected void seekToOffset(final long offset) throws IOException {
        if (randomAccessFile.getFilePointer() != offset) {
            randomAccessFile.seek(offset);
        }
    }

    @Override
    public Bytes zero() {
        try {
            randomAccessFile.setLength(0);
        } catch (IOException e) {
            throw new IOWrapException(e);
        }
        return this;
    }

    @Override
    public Bytes zero(long offset) {
        try {
            randomAccessFile.setLength(offset);
        } catch (IOException e) {
            throw new IOWrapException(e);
        }
        return this;
    }

    @Override
    public Bytes zero(final long offset, final long length) {
        for (long i = offset; i < offset + length; i++) {
            writeByte(i, (byte) 0);
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
        try {
            seekToOffset(position);
            ByteArray array = bytes.array(false);
            if (array != null) {
                return randomAccessFile.read(array.array(), (int) (array.offset() + offset), (int) size);
            } else {
                // 4K数组，如果都加载，可能内存不够
                byte[] data = new byte[SIZE_4K];
                int count;
                long pos = offset;
                int total = 0;
                while (size > 0) {
                    count = randomAccessFile.read(data, 0, data.length);
                    if (count == -1) {
                        break;
                    }
                    bytes.write(pos, data, 0, count);
                    size -= count;
                    pos += count;
                    total += count;
                }
                return total;
            }
        } catch (IOException e) {
            throw new IOWrapException(e);
        }
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
        try {
            seekToOffset(position);
            return randomAccessFile.read(bytes, (int) offset, (int) size);
        } catch (IOException e) {
            throw new IOWrapException(e);
        }
    }

    @Override
    public int readByte(final long offset) {
        checkRead(offset, BYTE);
        try {
            seekToOffset(offset);
            return randomAccessFile.readByte();
        } catch (IOException e) {
            throw new IOWrapException(e);
        }
    }

    @Override
    public char readChar(final long offset) {
        checkRead(offset, CHARACTER);
        try {
            seekToOffset(offset);
            return randomAccessFile.readChar();
        } catch (IOException e) {
            throw new IOWrapException(e);
        }
    }

    @Override
    public short readShort(final long offset) {
        checkRead(offset, SHORT);
        try {
            seekToOffset(offset);
            return randomAccessFile.readShort();
        } catch (IOException e) {
            throw new IOWrapException(e);
        }
    }

    @Override
    public int readMedium(final long offset) {
        checkRead(offset, MEDIUM);
        try {
            seekToOffset(offset);
            return (randomAccessFile.readByte()) << 16 | (randomAccessFile.readByte() & 0xff) << 8 | (randomAccessFile
                    .readByte() & 0xff);
        } catch (IOException e) {
            throw new IOWrapException(e);
        }
    }

    @Override
    public int readUnsignedMedium(final long offset) {
        checkRead(offset, MEDIUM);
        try {
            seekToOffset(offset);
            return (randomAccessFile.readByte() & 0xff) << 16 | (randomAccessFile
                    .readByte() & 0xff) << 8 | (randomAccessFile.readByte() & 0xff);
        } catch (IOException e) {
            throw new IOWrapException(e);
        }
    }

    @Override
    public int readInt(final long offset) {
        checkRead(offset, INTEGER);
        try {
            seekToOffset(offset);
            return randomAccessFile.readInt();
        } catch (IOException e) {
            throw new IOWrapException(e);
        }
    }

    @Override
    public long readLong(final long offset) {
        checkRead(offset, LONG);
        try {
            seekToOffset(offset);
            return randomAccessFile.readLong();
        } catch (IOException e) {
            throw new IOWrapException(e);
        }
    }

    @Override
    public Bytes write(final long position, final Bytes bytes, final long offset, final long length) {
        checkWrite(position, length);
        // TODO 测试
        ByteArray array = bytes.array(true);
        if (array != null) {
            // 支持数组拷贝
            try {
                seekToOffset(position);
                randomAccessFile.write(array.array(), (int) (offset + array.offset()), (int) length);
            } catch (IOException e) {
                throw new IOWrapException(e);
            }
        } else {
            try {
                seekToOffset(position);
                for (long i = 0; i < length; i++) {
                    randomAccessFile.writeByte(bytes.readByte(offset + i));
                }
            } catch (IOException e) {
                throw new IOWrapException(e);
            }
        }
        return this;
    }

    @Override
    public Bytes write(final long position, final byte[] bytes, final long offset, final long length) {
        checkWrite(position, length);
        try {
            seekToOffset(position);
            randomAccessFile.write(bytes, (int) offset, (int) length);
        } catch (IOException e) {
            throw new IOWrapException(e);
        }
        return this;
    }

    @Override
    public Bytes writeByte(final long offset, final int b) {
        checkWrite(offset, BYTE);
        try {
            seekToOffset(offset);
            randomAccessFile.writeByte(b);
        } catch (IOException e) {
            throw new IOWrapException(e);
        }
        return this;
    }

    @Override
    public Bytes writeChar(final long offset, final char c) {
        checkWrite(offset, CHARACTER);
        try {
            seekToOffset(offset);
            randomAccessFile.writeChar(c);
        } catch (IOException e) {
            throw new IOWrapException(e);
        }
        return this;
    }

    @Override
    public Bytes writeShort(final long offset, final short s) {
        checkWrite(offset, SHORT);
        try {
            seekToOffset(offset);
            randomAccessFile.writeShort(s);
        } catch (IOException e) {
            throw new IOWrapException(e);
        }
        return this;
    }

    @Override
    public Bytes writeMedium(final long offset, final int m) {
        checkWrite(offset, SHORT);
        try {
            seekToOffset(offset);
            randomAccessFile.writeByte((byte) (m >>> 16));
            randomAccessFile.writeByte((byte) (m >>> 8));
            randomAccessFile.writeByte((byte) m);
        } catch (IOException e) {
            throw new IOWrapException(e);
        }
        return this;
    }

    @Override
    public Bytes writeInt(final long offset, final int i) {
        checkWrite(offset, INTEGER);
        try {
            seekToOffset(offset);
            randomAccessFile.writeInt(i);
        } catch (IOException e) {
            throw new IOWrapException(e);
        }
        return this;
    }

    @Override
    public Bytes writeLong(final long offset, final long l) {
        checkWrite(offset, LONG);
        try {
            seekToOffset(offset);
            randomAccessFile.writeLong(l);
        } catch (IOException e) {
            throw new IOWrapException(e);
        }
        return this;
    }

    @Override
    public Bytes flush() {
        try {
            randomAccessFile.getFD().sync();
        } catch (IOException e) {
            throw new IOWrapException(e);
        }
        return this;
    }

    @Override
    public void close() {
        try {
            randomAccessFile.close();
        } catch (IOException e) {
            throw new IOWrapException(e);
        }
        super.close();
    }

    /**
     * Deletes the underlying file.
     */
    public void delete() {
        try {
            if (file.exists() && !file().delete()) {
                if (file.exists()) {
                    throw new IOException("delete file error." + file.getPath());
                }
            }
        } catch (IOException e) {
            throw new IOWrapException(e);
        }
    }

}
