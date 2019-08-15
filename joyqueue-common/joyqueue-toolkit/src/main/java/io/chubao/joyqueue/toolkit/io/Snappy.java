package io.chubao.joyqueue.toolkit.io;

import io.chubao.joyqueue.toolkit.io.snappy.SnappyFramedInputStream;
import io.chubao.joyqueue.toolkit.io.snappy.SnappyFramedOutputStream;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by hexiaofeng on 17-1-5.
 */
public class Snappy implements Compressor {
    public static final Compressor INSTANCE = new Snappy();

    @Override
    public void compress(final byte[] buf, final int offset, final int size, final OutputStream out) throws
            IOException {
        if (null == buf || out == null) {
            return;
        }
        SnappyFramedOutputStream sos = new SnappyFramedOutputStream(out);
        try {
            sos.write(buf, offset, size);
        } finally {
            sos.close();
        }
    }

    @Override
    public void decompress(final byte[] buf, final int offset, final int size, final OutputStream out) throws
            IOException {
        if (buf == null || buf.length == 0 || size <= 0 || offset >= buf.length || out == null) {
            return;
        }
        SnappyFramedInputStream sis = new SnappyFramedInputStream(new ByteArrayInputStream(buf, offset, size), false);
        try {
            byte[] buffer = new byte[1024];
            int position;
            while ((position = sis.read(buffer)) != -1) {
                out.write(buffer, 0, position);
            }
        } finally {
            sis.close();
        }
    }
}
