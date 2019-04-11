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

import com.jd.journalq.toolkit.buffer.bytes.HeapBytes;
import com.jd.journalq.toolkit.ref.ReferenceFactory;
import com.jd.journalq.toolkit.ref.ReferenceManager;

/**
 * Heap buffer pool.
 */
public class HeapBufferPool extends BufferPool {

    public HeapBufferPool() {
        super(new HeapBufferFactory());
    }

    @Override
    public void release(final Buffer reference) {
        reference.rewind();
        super.release(reference);
    }

    /**
     * Heap buffer factory.
     */
    static class HeapBufferFactory implements ReferenceFactory<Buffer> {
        @Override
        public Buffer create(final ReferenceManager<Buffer> manager) {
            HeapBuffer buffer = new HeapBuffer(HeapBytes.allocate(1024), manager);
            buffer.reset(0, 1024, Long.MAX_VALUE);
            return buffer;
        }
    }

}
