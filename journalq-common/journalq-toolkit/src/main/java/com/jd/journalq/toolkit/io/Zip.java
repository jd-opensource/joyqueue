/**
 *
 */
package com.jd.journalq.toolkit.io;

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
