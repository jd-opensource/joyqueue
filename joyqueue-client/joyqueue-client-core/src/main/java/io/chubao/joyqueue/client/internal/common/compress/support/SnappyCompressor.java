package io.chubao.joyqueue.client.internal.common.compress.support;

import io.chubao.joyqueue.client.internal.common.compress.Compressor;
import io.chubao.joyqueue.toolkit.io.snappy.SnappyFramedInputStream;
import io.chubao.joyqueue.toolkit.io.snappy.SnappyFramedOutputStream;

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