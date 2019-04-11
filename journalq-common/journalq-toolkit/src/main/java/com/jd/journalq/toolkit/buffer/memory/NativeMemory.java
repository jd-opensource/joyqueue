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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteOrder;

/**
 * Native memory. Represents memory that can be accessed directly via {@link sun.misc.Unsafe}
 */
public abstract class NativeMemory<M extends NativeMemory, T extends MemoryAllocator<M>> extends AbstractMemory<T> {
    protected static final boolean UNALIGNED;
    protected static final boolean BIG_ENDIAN = ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN;

    static {
        boolean unaligned;
        try {
            Class<?> bitsClass = Class.forName("java.nio.Bits", false, ClassLoader.getSystemClassLoader());
            Method unalignedMethod = bitsClass.getDeclaredMethod("unaligned");
            unalignedMethod.setAccessible(true);
            unaligned = Boolean.TRUE.equals(unalignedMethod.invoke(null));
        } catch (ClassNotFoundException e) {
            unaligned = false;
        } catch (NoSuchMethodException e) {
            unaligned = false;
        } catch (IllegalAccessException e) {
            unaligned = false;
        } catch (InvocationTargetException e) {
            unaligned = false;
        }
        UNALIGNED = unaligned;
    }

    @SuppressWarnings("unchecked")
    protected NativeMemory(final long address, final long size, final T allocator) {
        super(allocator);
        Preconditions.checkArgument(size >= 0 && size <= SIZE_MAX, SIZE_ERROR);
        this.address = address;
        this.size = size;
        this.allocator = allocator;
    }

    @Override
    public M copy() {
        M memory = allocator.allocate(size);
        UNSAFE.copyMemory(address, memory.address, size);
        return memory;
    }

    @Override
    public byte getByte(final long offset) {
        return UNSAFE.getByte(address(offset));
    }

    @Override
    public char getChar(final long offset) {
        if (UNALIGNED) {
            return UNSAFE.getChar(address(offset));
        } else if (BIG_ENDIAN) {
            return (char) (getByte(offset) << 8 | getByte(offset + 1) & 0xff);
        } else {
            return (char) ((getByte(offset + 1) << 8) | getByte(offset) & 0xff);
        }
    }

    @Override
    public short getShort(final long offset) {
        if (UNALIGNED) {
            return UNSAFE.getShort(address(offset));
        } else if (BIG_ENDIAN) {
            return (short) (getByte(offset) << 8 | getByte(offset + 1) & 0xff);
        } else {
            return (short) ((getByte(offset + 1) << 8) | getByte(offset) & 0xff);
        }
    }

    @Override
    public int getInt(final long offset) {
        if (UNALIGNED) {
            return UNSAFE.getInt(address(offset));
        } else if (BIG_ENDIAN) {
            return (getByte(offset)) << 24 | (getByte(offset + 1) & 0xff) << 16 | (getByte(
                    offset + 2) & 0xff) << 8 | (getByte(offset + 3) & 0xff);
        } else {
            return (getByte(offset + 3)) << 24 | (getByte(offset + 2) & 0xff) << 16 | (getByte(
                    offset + 1) & 0xff) << 8 | (getByte(offset) & 0xff);
        }
    }

    @Override
    public long getLong(final long offset) {
        if (UNALIGNED) {
            return UNSAFE.getLong(address(offset));
        } else if (BIG_ENDIAN) {
            return ((long) getByte(offset)) << 56 | ((long) getByte(offset + 1) & 0xff) << 48 | ((long) getByte(
                    offset + 2) & 0xff) << 40 | ((long) getByte(offset + 3) & 0xff) << 32 | ((long) getByte(
                    offset + 4) & 0xff) << 24 | ((long) getByte(offset + 5) & 0xff) << 16 | ((long) getByte(
                    offset + 6) & 0xff) << 8 | ((long) getByte(offset + 7) & 0xff);
        } else {
            return ((long) getByte(offset + 7)) << 56 | ((long) getByte(offset + 6) & 0xff) << 48 | ((long) getByte(
                    offset + 5) & 0xff) << 40 | ((long) getByte(offset + 4) & 0xff) << 32 | ((long) getByte(
                    offset + 3) & 0xff) << 24 | ((long) getByte(offset + 2) & 0xff) << 16 | ((long) getByte(
                    offset + 1) & 0xff) << 8 | ((long) getByte(offset) & 0xff);
        }
    }

    @Override
    public void putByte(final long offset, final byte b) {
        UNSAFE.putByte(address(offset), b);
    }

    @Override
    public void putChar(final long offset, final char c) {
        if (UNALIGNED) {
            putChar(address(offset), c);
        } else if (BIG_ENDIAN) {
            putByte(offset, (byte) (c >>> 8));
            putByte(offset + 1, (byte) c);
        } else {
            putByte(offset + 1, (byte) (c >>> 8));
            putByte(offset, (byte) c);
        }
    }

    @Override
    public void putShort(final long offset, final short s) {
        if (UNALIGNED) {
            UNSAFE.putShort(address(offset), s);
        } else if (BIG_ENDIAN) {
            putByte(offset, (byte) (s >>> 8));
            putByte(offset + 1, (byte) s);
        } else {
            putByte(offset + 1, (byte) (s >>> 8));
            putByte(offset, (byte) s);
        }
    }

    @Override
    public void putInt(final long offset, final int i) {
        if (UNALIGNED) {
            UNSAFE.putInt(address(offset), i);
        } else if (BIG_ENDIAN) {
            putByte(offset, (byte) (i >>> 24));
            putByte(offset + 1, (byte) (i >>> 16));
            putByte(offset + 2, (byte) (i >>> 8));
            putByte(offset + 3, (byte) i);
        } else {
            putByte(offset + 3, (byte) (i >>> 24));
            putByte(offset + 2, (byte) (i >>> 16));
            putByte(offset + 1, (byte) (i >>> 8));
            putByte(offset, (byte) i);
        }
    }

    @Override
    public void putLong(final long offset, final long l) {
        if (UNALIGNED) {
            UNSAFE.putLong(address(offset), l);
        } else if (BIG_ENDIAN) {
            putByte(offset, (byte) (l >>> 56));
            putByte(offset + 1, (byte) (l >>> 48));
            putByte(offset + 2, (byte) (l >>> 40));
            putByte(offset + 3, (byte) (l >>> 32));
            putByte(offset + 4, (byte) (l >>> 24));
            putByte(offset + 5, (byte) (l >>> 16));
            putByte(offset + 6, (byte) (l >>> 8));
            putByte(offset + 7, (byte) l);
        } else {
            putByte(offset + 7, (byte) (l >>> 56));
            putByte(offset + 6, (byte) (l >>> 48));
            putByte(offset + 5, (byte) (l >>> 40));
            putByte(offset + 4, (byte) (l >>> 32));
            putByte(offset + 3, (byte) (l >>> 24));
            putByte(offset + 2, (byte) (l >>> 16));
            putByte(offset + 1, (byte) (l >>> 8));
            putByte(offset, (byte) l);
        }
    }

    @Override
    public void clear() {
        UNSAFE.setMemory(address, size, (byte) 0);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void free() {
        if (address != 0) {
            NativeMemory.UNSAFE.freeMemory(address);
            address = 0;
        }
    }

}
