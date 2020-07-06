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
package org.joyqueue.toolkit.io;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * zip压缩工具类
 *
 * @author hexiaofeng
 */
public class Zip implements Compressor {
    public static final Compressor INSTANCE = new Zip();

    @Override
    public void compress(final byte[] buf, final int offset, final int size, final OutputStream out) throws
            IOException {
        if (buf == null || out == null) {
            return;
        }
        ZipOutputStream zos = new ZipOutputStream(out);
        try {
            zos.putNextEntry(new ZipEntry("0"));
            zos.write(buf, offset, size);
            zos.closeEntry();
        } finally {
            zos.close();
        }
    }

    @Override
    public void decompress(final byte[] buf, final int offset, final int size, final OutputStream out) throws
            IOException {
        if (buf == null || buf.length == 0 || size <= 0 || offset >= buf.length || out == null) {
            return;
        }
        ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(buf, offset, size));
        try {
            zis.getNextEntry();
            byte[] buffer = new byte[1024];
            int position = -1;
            while ((position = zis.read(buffer)) != -1) {
                out.write(buffer, 0, position);
            }
        } finally {
            zis.close();
        }
    }

}
