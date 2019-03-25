package com.jd.journalq.toolkit.buffer;

import com.jd.journalq.toolkit.buffer.bytes.HeapBytes;
import com.jd.journalq.toolkit.buffer.bytes.MappedBytes;
import com.jd.journalq.toolkit.lang.Preconditions;

/**
 * Direct memory bit set.
 * <p>
 * The direct bit set performs bitwise operations on a fixed count {@link HeapBytes} instance.
 * Currently, all bytes are {@link HeapBytes}, but theoretically {@link MappedBytes}
 * could be used for durability as well.
 */
public class BitArray {

    /**
     * Allocates a new direct bit set.
     *
     * @param bits The number of bits in the bit set.
     * @return The allocated bit set.
     */
    public static BitArray allocate(final long bits) {
        Preconditions.checkArgument(bits > 0 & (bits & (bits - 1)) == 0, "size must be a power of 2");
        return new BitArray(HeapBytes.allocate(Math.max(bits / 8 + 8, 8)), bits);
    }

    private final HeapBytes bytes;
    private long size;
    private long count;

    BitArray(final HeapBytes bytes, final long size) {
        this(bytes, 0, size);
    }

    BitArray(final HeapBytes bytes, final long count, final long size) {
        this.bytes = bytes;
        this.size = size;
        this.count = count;
    }

    /**
     * Returns the offset of the long that stores the bit for the given index.
     */
    private long offset(final long index) {
        return (index / 64) * 8;
    }

    /**
     * Returns the position of the bit for the given index.
     */
    private long position(final long index) {
        return index % 64;
    }

    /**
     * Sets the bit at the given index.
     *
     * @param index The index of the bit to set.
     * @return Indicates if the bit was changed.
     */
    public boolean set(final long index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException();
        }
        if (!get(index)) {
            bytes.writeLong(offset(index), bytes.readLong(offset(index)) | (1l << position(index)));
            count++;
            return true;
        }
        return false;
    }

    /**
     * Gets the bit at the given index.
     *
     * @param index The index of the bit to get.
     * @return Indicates whether the bit is set.
     */
    public boolean get(final long index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException();
        }
        return (bytes.readLong(offset(index)) & (1l << (position(index)))) != 0;
    }

    /**
     * Returns the total number of bits in the bit set.
     *
     * @return The total number of bits in the bit set.
     */
    public long size() {
        return size;
    }

    /**
     * Returns the number of bits set in the bit set.
     *
     * @return The number of bits set.
     */
    public long count() {
        return count;
    }

    /**
     * Resizes the bit array to a new count.
     *
     * @param size The new count.
     * @return The resized bit array.
     */
    public BitArray resize(final long size) {
        Preconditions.checkArgument(size > 0 & (size & (size - 1)) == 0, "size must be a power of 2");
        bytes.resize(Math.max(size / 8 + 8, 8));
        this.size = size;
        return this;
    }

    /**
     * Copies the bit set to a new memory address.
     *
     * @return The copied bit set.
     */
    public BitArray copy() {
        return new BitArray(bytes.copy(), count, size);
    }

    public void close() {
        bytes.close();
    }

    @Override
    public String toString() {
        return String.format("%s[size=%d]", getClass().getSimpleName(), size);
    }

}
