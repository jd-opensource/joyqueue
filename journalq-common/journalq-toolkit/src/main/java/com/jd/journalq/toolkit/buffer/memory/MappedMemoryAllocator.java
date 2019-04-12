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

import com.jd.journalq.toolkit.buffer.IOWrapException;
import com.jd.journalq.toolkit.lang.Preconditions;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Mapped memory allocator.
 * <p>
 * The mapped memory allocator provides direct memory access to memory mapped from a file on disk. The mapped allocator
 * supports allocating memory in any {@link java.nio.channels.FileChannel.MapMode}. Once the file is mapped and the
 * memory has been allocated, the mapped allocator provides the memory address of the underlying
 * {@link java.nio.MappedByteBuffer} for access via {@link sun.misc.Unsafe}.
 */
public class MappedMemoryAllocator implements MemoryAllocator<MappedMemory> {
    public static final FileChannel.MapMode DEFAULT_MAP_MODE = FileChannel.MapMode.READ_WRITE;
    private static final String MAX_SIZE_ERROR_MESSAGE =
            "mapped memory size cannot be greater than " + MappedMemory.SIZE_MAX;

    private final AtomicInteger referenceCount = new AtomicInteger();
    private final RandomAccessFile file;
    private final FileChannel channel;
    private final FileChannel.MapMode mode;
    private final long offset;

    public MappedMemoryAllocator(File file) {
        this(file, DEFAULT_MAP_MODE, 0);
    }

    public MappedMemoryAllocator(File file, FileChannel.MapMode mode) {
        this(file, mode, 0);
    }

    public MappedMemoryAllocator(File file, FileChannel.MapMode mode, long offset) {
        this(createFile(file, mode), mode, offset);
    }

    public MappedMemoryAllocator(RandomAccessFile file, FileChannel.MapMode mode, long offset) {
        Preconditions.checkNotNull(file, "file cannot be null");
        Preconditions.checkNotNull(mode, "mode cannot be null");
        Preconditions.checkArgument(offset >= 0, "offset cannot be negative");
        this.file = file;
        this.channel = this.file.getChannel();
        this.mode = mode;
        this.offset = offset;
    }

    static RandomAccessFile createFile(final File file, final FileChannel.MapMode mode) {
        Preconditions.checkNotNull(file, "file cannot be null");
        try {
            return new RandomAccessFile(file, parseMode(mode == null ? DEFAULT_MAP_MODE : mode));
        } catch (IOException e) {
            throw new IOWrapException(e);
        }
    }

    static String parseMode(final FileChannel.MapMode mode) {
        if (mode == FileChannel.MapMode.READ_ONLY) {
            return "r";
        } else if (mode == FileChannel.MapMode.READ_WRITE) {
            return "rw";
        }
        throw new IllegalArgumentException("unsupported map mode");
    }

    @Override
    public MappedMemory allocate(final long size) {
        Preconditions.checkArgument(size >= 0 && size <= MappedMemory.SIZE_MAX, MAX_SIZE_ERROR_MESSAGE);
        try {
            if (file.length() < size) {
                file.setLength(size);
            }
            referenceCount.incrementAndGet();
            return new MappedMemory(channel.map(mode, offset, size), this);
        } catch (IOException e) {
            throw new IOWrapException(e);
        }
    }

    @Override
    public MappedMemory reallocate(final MappedMemory memory, final long size) {
        MappedMemory newMemory = allocate(size);
        memory.free();
        return newMemory;
    }

    /**
     * Releases a reference from the allocator.
     */
    public void release() {
        if (referenceCount.decrementAndGet() == 0) {
            try {
                file.close();
            } catch (IOException e) {
                throw new IOWrapException(e);
            }
        }
    }

}
