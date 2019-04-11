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
import com.jd.journalq.toolkit.os.Systems;

/**
 * Java heap memory allocator.
 */
public class HeapMemoryAllocator implements MemoryAllocator<HeapMemory> {

    public static final HeapMemoryAllocator INSTANCE = new HeapMemoryAllocator();

    @Override
    public HeapMemory allocate(final long size) {
        Preconditions.checkArgument(size >= 0 && size <= Integer.MAX_VALUE, HeapMemory.SIZE_ERROR);
        return new HeapMemory(new byte[(int) size], this);
    }

    @Override
    public HeapMemory reallocate(final HeapMemory memory, final long size) {
        HeapMemory copy = allocate(size);
        Systems.UNSAFE
                .copyMemory(memory.memory(), HeapMemory.ARRAY_BASE_OFFSET, copy.memory(), HeapMemory.ARRAY_BASE_OFFSET,
                        Math.min(size, memory.size()));
        memory.free();
        return copy;
    }

}
