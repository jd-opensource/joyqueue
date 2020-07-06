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
package org.joyqueue.client.internal.common.compress;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * CompressUtils
 *
 * author: gaohaoxiang
 * date: 2019/1/2
 */
public class CompressUtils {

    public static byte[] compress(byte[] bytes, String compressType) throws IOException {
        Compressor compressor = CompressorManager.getCompressor(compressType);
        if (compressor == null) {
            throw new IllegalArgumentException(String.format("compressor %s not exist", compressor));
        }
        return compress(bytes, compressor);
    }

    public static byte[] decompress(byte[] bytes, String compressType) throws IOException {
        Compressor compressor = CompressorManager.getCompressor(compressType);
        if (compressor == null) {
            throw new IllegalArgumentException(String.format("compressor %s not exist", compressor));
        }
        return decompress(bytes, compressor);
    }

    public static byte[] compress(byte[] bytes, Compressor compressor) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream(bytes.length);
        try {
            compress(bytes, 0, bytes.length, bos, compressor);
            return bos.toByteArray();
        } finally {
            bos.close();
        }
    }

    public static byte[] decompress(byte[] bytes, Compressor compressor) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream(bytes.length * 4);
        try {
            decompress(bytes, 0, bytes.length, bos, compressor);
            return bos.toByteArray();
        } finally {
            bos.close();
        }
    }

    public static void compress(byte[] bytes, int offset, int size, OutputStream out, Compressor compressor) throws IOException {
        compressor.compress(bytes, offset, size, out);
    }

    public static void decompress(byte[] bytes, int offset, int size, OutputStream out, Compressor compressor) throws IOException {
        compressor.decompress(bytes, offset, size, out);
    }
}