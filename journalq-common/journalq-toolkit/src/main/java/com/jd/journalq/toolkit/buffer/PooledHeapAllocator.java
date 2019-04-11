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
package com.jd.journalq.toolkit.buffer;

import com.jd.journalq.toolkit.buffer.memory.HeapMemory;
import com.jd.journalq.toolkit.ref.ReferencePool;

/**
 * Pooled heap buffer allocator.
 */
public class PooledHeapAllocator extends PooledAllocator {

    public static final PooledHeapAllocator INSTANCE = new PooledHeapAllocator();

    @SuppressWarnings({"unchecked", "rawtypes"})
    public PooledHeapAllocator() {
        super((ReferencePool) new HeapBufferPool());
    }

    @Override
    protected long maxCapacity() {
        return HeapMemory.SIZE_MAX;
    }

}
