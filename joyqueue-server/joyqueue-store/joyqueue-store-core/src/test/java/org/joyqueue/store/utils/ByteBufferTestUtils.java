/**
 * Copyright 2019 The JoyQueue Authors.
 *
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
package org.joyqueue.store.utils;

import org.joyqueue.toolkit.buffer.RByteBuffer;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * @author liyue25
 * Date: 2018/9/18
 */
public class ByteBufferTestUtils {
    /**
     * 将多个rbytebuffer拷贝到一个buffer中。
     * 内存拷贝，效率低，只能用于单元测试。
     */
    public static ByteBuffer concat(List<RByteBuffer> byteBuffers) {
        int size = byteBuffers.stream().mapToInt(RByteBuffer::remaining).sum();
        if (size > 0) {
            ByteBuffer buffer = ByteBuffer.allocate(size);
            ByteBuffer[] array = byteBuffers.stream().map(RByteBuffer::getBuffer).toArray(ByteBuffer[]::new);
            ByteBufferUtils.concat(buffer, array);
            buffer.flip();
            byteBuffers.forEach(RByteBuffer::close);
            return buffer;
        } else {
            return null;
        }
    }
}
