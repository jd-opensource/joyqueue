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


import com.jd.journalq.toolkit.buffer.IOWrapException;
import com.jd.journalq.toolkit.buffer.memory.MappedMemory;
import com.jd.journalq.toolkit.buffer.memory.MappedMemoryAllocator;
import com.jd.journalq.toolkit.lang.Preconditions;

import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;

/**
 * Mapped bytes.
 * <p>
 * Mapped bytes provide direct access to memory from allocated by a {@link java.nio.MappedByteBuffer}. Memory is
 * allocated
 * by opening and expanding the given {@link java.io.File} to the desired {@code count} and mapping the file contents
 * into memory
 * via {@link java.nio.channels.FileChannel#map(java.nio.channels.FileChannel.MapMode, long, long)}.
 * <p>
 * Closing the bytes via {@link Bytes#close()} will result in {@link Bytes#flush()}
 * being automatically called.
 */
public class MappedBytes extends NativeBytes<MappedMemory> {

    private static final String MAX_SIZE_ERROR_MESSAGE =
            "size for MappedBytes cannot be greater than " + MappedMemory.SIZE_MAX;

    /**
     * Allocates a mapped buffer in {@link java.nio.channels.FileChannel.MapMode#READ_WRITE} mode.
     * <p>
     * Memory will be mapped by opening and expanding the given {@link java.io.File} to the desired {@code count} and
     * mapping the
     * file contents into memory via
     * {@link java.nio.channels.FileChannel#map(java.nio.channels.FileChannel.MapMode, long, long)}.
     *
     * @param file The file to map into memory. If the file doesn't exist it will be automatically created.
     * @param size The count of the buffer to allocate (in bytes).
     * @return The mapped buffer.
     * @throws NullPointerException     If {@code file} is {@code null}
     * @throws IllegalArgumentException If {@code count} is greater than {@link MappedMemory#SIZE_MAX}
     * @see MappedBytes#allocate(java.io.File, java.nio.channels.FileChannel.MapMode, long)
     */
    public static MappedBytes allocate(final File file, final long size) {
        return allocate(file, MappedMemoryAllocator.DEFAULT_MAP_MODE, size);
    }

    /**
     * Allocates a mapped buffer.
     * <p>
     * Memory will be mapped by opening and expanding the given {@link java.io.File} to the desired {@code count} and
     * mapping the
     * file contents into memory via
     * {@link java.nio.channels.FileChannel#map(java.nio.channels.FileChannel.MapMode, long, long)}.
     *
     * @param file The file to map into memory. If the file doesn't exist it will be automatically created.
     * @param mode The mode with which to map the file.
     * @param size The count of the buffer to allocate (in bytes).
     * @return The mapped buffer.
     * @throws NullPointerException     If {@code file} is {@code null}
     * @throws IllegalArgumentException If {@code count} is greater than {@link MappedMemory#SIZE_MAX}
     * @see MappedBytes#allocate(java.io.File, long)
     */
    public static MappedBytes allocate(final File file, final FileChannel.MapMode mode, final long size) {
        Preconditions.checkNotNull(file, "file cannot be null");
        Preconditions.checkArgument(size <= MappedMemory.SIZE_MAX, MAX_SIZE_ERROR_MESSAGE);
        return new MappedBytes(file,
                MappedMemory.allocate(file, mode == null ? MappedMemoryAllocator.DEFAULT_MAP_MODE : mode, size));
    }

    protected final File file;

    public MappedBytes(File file, MappedMemory memory) {
        super(memory);
        this.file = file;
    }

    @Override
    public boolean isDirect() {
        return true;
    }

    @Override
    public boolean isFile() {
        return true;
    }

    @Override
    public Bytes flush() {
        memory.flush();
        return this;
    }

    /**
     * Deletes the underlying file.
     */
    public void delete() {
        try {
            if (!file.delete()) {
                throw new IOException("delete file error.");
            }
        } catch (IOException e) {
            throw new IOWrapException(e);
        }
    }

}
