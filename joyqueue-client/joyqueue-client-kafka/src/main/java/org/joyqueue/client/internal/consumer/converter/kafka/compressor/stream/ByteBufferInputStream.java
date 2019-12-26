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
package org.joyqueue.client.internal.consumer.converter.kafka.compressor.stream;

import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * Created by zhangkepeng on 16-8-30.
 */
public class ByteBufferInputStream extends InputStream {

    private ByteBuffer buffer;

    public ByteBufferInputStream(ByteBuffer buffer) {
        this.buffer = buffer;
    }

    @Override
    public int read() {
        if (buffer != null && buffer.hasRemaining()) {
            return (buffer.get() & 0xFF);
        } else {
            return -1;
        }
    }

    @Override
    public int read(byte[] bytes, int off, int len) {
        if (buffer != null && buffer.hasRemaining()) {
            int realLen = Math.min(len, buffer.remaining());
            buffer.get(bytes, off, realLen);
            return realLen;
        } else {
            return -1;
        }
    }
}
