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
package org.joyqueue.client.internal.common.compress.support;

import org.joyqueue.client.internal.common.compress.Compressor;
import org.joyqueue.toolkit.io.snappy.SnappyFramedInputStream;
import org.joyqueue.toolkit.io.snappy.SnappyFramedOutputStream;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * SnappyCompressor
 *
 * author: gaohaoxiang
 * date: 2019/4/2
 */
public class SnappyCompressor implements Compressor {

    public static final String NAME = "snappy";

    private static final int BUFFER_SIZE = 1024;

    @Override
    public void compress(byte[] bytes, int offset, int size, OutputStream out) throws IOException {
        SnappyFramedOutputStream sos = new SnappyFramedOutputStream(out);
        try {
            sos.write(bytes, offset, size);
        } finally {
            sos.close();
        }
    }

    @Override
    public void decompress(byte[] bytes, int offset, int size, OutputStream out) throws IOException {
        SnappyFramedInputStream sis = new SnappyFramedInputStream(new ByteArrayInputStream(bytes, offset, size), false);
        try {
            byte[] buffer = new byte[BUFFER_SIZE];
            int position;
            while ((position = sis.read(buffer)) != -1) {
                out.write(buffer, 0, position);
            }
        } finally {
            sis.close();
        }
    }

    @Override
    public String type() {
        return NAME;
    }
}