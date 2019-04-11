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
package com.jd.journalq.toolkit.buffer.memory;

import com.jd.journalq.toolkit.lang.Preconditions;

/**
 * Java heap memory.
 */
public class HeapMemory extends AbstractMemory<HeapMemoryAllocator> {


    /**
     * Allocates heap memory via {@link HeapMemoryAllocator}.
     *
     * @param size The count of the memory to allocate.
     * @return The allocated memory.
     * @throws IllegalArgumentException If {@code count} is greater than the maximum allowed count for
     *                                  an array on the Java heap - {@code Integer.MAX_VALUE - 5}
     */
    public static HeapMemory allocate(final long size) {
        return HeapMemoryAllocator.INSTANCE.allocate(size);
    }

    /**
     * Wraps the given bytes in a {@link HeapMemory} object.
     *
     * @param bytes The bytes to wrap.
     * @return The wrapped bytes.
     */
    public static HeapMemory wrap(final byte[] bytes) {
        return new HeapMemory(bytes, HeapMemoryAllocator.INSTANCE);
    }

    protected byte[] memory;

    public HeapMemory(final byte[] memory, final HeapMemoryAllocator allocator) {
        super(allocator);
        Preconditions.checkNotNull(memory, "array cannot be null");
        this.memory = memory;
        this.size = memory.length;
        this.address = ARRAY_BASE_OFFSET;
    }

    public byte[] memory() {
        return memory;
    }

    /**
     * Resets the memory pointer.
     *
     * @param array The memory array.
     * @return The heap memory.
     */
    public HeapMemory reset(final byte[] array) {
        Preconditions.checkNotNull(array, "array cannot be null");
        this.memory = array;
        this.size = array.length;
        return this;
    }

    @Override
    public long size() {
        return memory.length;
    }

    @Override
    public HeapMemory copy() {
        HeapMemory copy = allocator.allocate(memory.length);
        UNSAFE.copyMemory(memory, ARRAY_BASE_OFFSET, copy.memory, ARRAY_BASE_OFFSET, memory.length);
        return copy;
    }

    @Override
    public byte getByte(final long offset) {
        return UNSAFE.getByte(memory, address(offset));
    }

    @Override
    public char getChar(final long offset) {
        return UNSAFE.getChar(memory, address(offset));
    }

    @Override
    public short getShort(final long offset) {
        return UNSAFE.getShort(memory, address(offset));
    }

    @Override
    public int getInt(final long offset) {
        return UNSAFE.getInt(memory, address(offset));
    }

    @Override
    public long getLong(final long offset) {
        return UNSAFE.getLong(memory, address(offset));
    }

    @Override
    public void putByte(final long offset, final byte b) {
        UNSAFE.putByte(memory, address(offset), b);
    }

    @Override
    public void putChar(final long offset, final char c) {
        UNSAFE.putChar(memory, address(offset), c);
    }

    @Override
    public void putShort(final long offset, final short s) {
        UNSAFE.putShort(memory, address(offset), s);
    }

    @Override
    public void putInt(final long offset, final int i) {
        UNSAFE.putInt(memory, address(offset), i);
    }

    @Override
    public void putLong(final long offset, final long l) {
        UNSAFE.putLong(memory, address(offset), l);
    }

    @Override
    public void clear() {
        for (int i = 0; i < memory.length; i++) {
            memory[i] = 0;
        }
        // JDK6 没有这个函数
        // UNSAFE.setMemory(array, ARRAY_BASE_OFFSET, array.length, (byte) 0);
    }

    @Override
    public void free() {
        clear();
    }

}
