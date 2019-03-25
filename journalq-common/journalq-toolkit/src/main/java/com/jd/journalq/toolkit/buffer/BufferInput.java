package com.jd.journalq.toolkit.buffer;

import com.jd.journalq.toolkit.buffer.bytes.Bytes;

/**
 * Readable buffer.
 * <p>
 * This interface exposes methods for reading from a byte buffer. Readable buffers maintain a small amount of state
 * regarding current cursor positions and limits similar to the behavior of {@link java.nio.ByteBuffer}.
 */
public interface BufferInput {

    /**
     * Returns the number of bytes remaining in the input.
     *
     * @return The number of bytes remaining in the input.
     */
    long remaining();

    /**
     * Returns a boolean value indicating whether the input has bytes remaining.
     *
     * @return Indicates whether bytes remain to be read from the input.
     */
    boolean hasRemaining();

    /**
     * Skips the given number of bytes in the input.
     *
     * @param bytes The number of bytes to attempt to skip.
     * @return The actual skipped size.
     */
    long skip(long bytes);

    /**
     * Reads bytes into the given byte array.
     *
     * @param bytes The byte array into which to read bytes.
     * @return Actual size.
     */
    long read(Bytes bytes);

    /**
     * Reads bytes into the given byte array.
     *
     * @param bytes The byte array into which to read bytes.
     * @return Actual size.
     */
    long read(byte[] bytes);

    /**
     * Reads bytes into the given byte array starting at the current position.
     *
     * @param bytes  The byte array into which to read bytes.
     * @param offset The offset at which to write bytes into the given buffer
     * @param length The max number of bytes to read.
     * @return The buffer.
     */
    long read(Bytes bytes, long offset, long length);

    /**
     * Reads bytes into the given byte array starting at current position up to the given length.
     *
     * @param bytes  The byte array into which to read bytes.
     * @param offset The offset at which to write bytes into the given buffer
     * @param length The max number of bytes to read.
     * @return Actual size.
     */
    long read(byte[] bytes, long offset, long length);

    /**
     * Reads bytes into the given buffer.
     *
     * @param buffer The buffer into which to read bytes.
     * @return Actual size.
     */
    long read(Buffer buffer);

    /**
     * Reads a byte from the buffer at the current position.
     *
     * @return The read byte.
     */
    int readByte();

    /**
     * Reads an unsigned byte from the buffer at the current position.
     *
     * @return The read byte.
     */
    int readUnsignedByte();

    /**
     * Reads a 16-bit character from the buffer at the current position.
     *
     * @return The read character.
     */
    char readChar();

    /**
     * Reads a 16-bit signed integer from the buffer at the current position.
     *
     * @return The read short.
     */
    short readShort();

    /**
     * Reads a 16-bit unsigned integer from the buffer at the current position.
     *
     * @return The read short.
     */
    int readUnsignedShort();

    /**
     * Reads a 24-bit signed integer from the buffer at the current position.
     *
     * @return The read integer.
     */
    int readMedium();

    /**
     * Reads a 24-bit unsigned integer from the buffer at the current position.
     *
     * @return The read integer.
     */
    int readUnsignedMedium();

    /**
     * Reads a 32-bit signed integer from the buffer at the current position.
     *
     * @return The read integer.
     */
    int readInt();

    /**
     * Reads a 32-bit unsigned integer from the buffer at the current position.
     *
     * @return The read integer.
     */
    long readUnsignedInt();

    /**
     * Reads a 64-bit signed integer from the buffer at the current position.
     *
     * @return The read long.
     */
    long readLong();

    /**
     * Reads a single-precision 32-bit floating point number from the buffer at the current position.
     *
     * @return The read float.
     */
    float readFloat();

    /**
     * Reads a double-precision 64-bit floating point number from the buffer at the current position.
     *
     * @return The read double.
     */
    double readDouble();

    /**
     * Reads a 1 byte boolean from the buffer at the current position.
     *
     * @return The read boolean.
     */
    boolean readBoolean();

    /**
     * Reads a string from the buffer at the current position.
     *
     * @return The read string.
     */
    String readString();

    /**
     * Reads a UTF-8 string from the buffer at the current position.
     *
     * @return The read string.
     */
    String readUTF8();

    /**
     * 关闭
     */
    void close();

}
