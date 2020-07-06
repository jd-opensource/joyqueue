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
import org.joyqueue.toolkit.io.ZipDeflater;
import org.joyqueue.toolkit.io.ZipInflater;

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.DataFormatException;

/**
 * ZlibCompressor
 *
 * author: gaohaoxiang
 * date: 2019/1/2
 */
public class ZlibCompressor implements Compressor {

    public static final String NAME = "zlib";

    @Override
    public void compress(byte[] bytes, int offset, int size, OutputStream out) throws IOException {
        ZipDeflater deflater = new ZipDeflater();
        deflater.reset();
        deflater.setInput(bytes, offset, size);
        deflater.finish();
        int len;
        byte[] block = new byte[1024];
        try {
            while (!deflater.finished()) {
                len = deflater.deflate(block);
                out.write(block, 0, len);
            }
        } finally {
            deflater.end();
        }
    }

    @Override
    public void decompress(byte[] bytes, int offset, int size, OutputStream out) throws IOException {
        ZipInflater inflater = new ZipInflater();
        inflater.reset();
        inflater.setInput(bytes, offset, size);
        int len;
        byte[] block = new byte[1024];
        try {
            while (!inflater.finished()) {
                len = inflater.inflate(block);
                out.write(block, 0, len);
            }
        } catch (DataFormatException e) {
            throw new IOException(e);
        } finally {
            inflater.end();
        }
    }

    @Override
    public String type() {
        return NAME;
    }
}