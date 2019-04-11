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
package com.jd.journalq.toolkit.buffer.stream;

import com.jd.journalq.toolkit.buffer.BufferOutput;

import java.io.IOException;
import java.io.OutputStream;

/**
 * 缓冲器输出流
 */
public class BufferOutputStream extends OutputStream {
    protected final BufferOutput<?> buffer;

    public BufferOutputStream(BufferOutput<?> buffer) {
        this.buffer = buffer;
    }

    @Override
    public void write(final int b) throws IOException {
        buffer.writeByte(b);
    }

    @Override
    public void write(final byte[] b) throws IOException {
        buffer.write(b);
    }

    @Override
    public void write(final byte[] b, final int off, final int len) throws IOException {
        buffer.write(b, off, len);
    }

    @Override
    public void flush() throws IOException {
        buffer.flush();
    }

    @Override
    public void close() throws IOException {
        buffer.close();
    }

}
