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

import com.jd.journalq.toolkit.buffer.bytes.NativeBytes;
import com.jd.journalq.toolkit.buffer.memory.NativeMemory;
import com.jd.journalq.toolkit.ref.Reference;
import com.jd.journalq.toolkit.ref.ReferenceManager;

/**
 * Native byte buffer implementation.
 */
public abstract class NativeBuffer extends AbstractBuffer {

    public NativeBuffer(NativeBytes bytes, ReferenceManager<Buffer> referenceManager) {
        super(bytes, referenceManager);
    }

    public NativeBuffer(NativeBytes bytes, Reference reference, ReferenceManager<Buffer> referenceManager) {
        super(bytes, reference, referenceManager);
    }

    public NativeBuffer(NativeBytes bytes, long offset, long initialCapacity, long maxCapacity) {
        super(bytes, offset, initialCapacity, maxCapacity);
    }

    public NativeBuffer(NativeBytes bytes, long offset, long initialCapacity, long maxCapacity, Reference reference,
            ReferenceManager<Buffer> referenceManager) {
        super(bytes, offset, initialCapacity, maxCapacity, reference, referenceManager);
    }

    @Override
    protected void compact(final long from, final long to, final long length) {
        NativeMemory memory = ((NativeBytes) bytes).memory();
        UNSAFE.copyMemory(memory.address(from), memory.address(to), length);
        UNSAFE.setMemory(memory.address(from), length, (byte) 0);
    }

}
