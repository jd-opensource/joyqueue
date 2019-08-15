package io.chubao.joyqueue.toolkit.io;

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

/**
 * Zlib压缩
 * Created by hexiaofeng on 16-5-6.
 */
public class Zlib implements Compressor {

    public static final Compressor INSTANCE = new Zlib();

    @Override
    public void compress(final byte[] buf, int offset, final int size, final OutputStream out) throws IOException {
        if (buf == null || out == null) {
            return;
        }
        Deflater deflater = new Deflater();
        deflater.reset();
        deflater.setInput(buf, offset, size);
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
    public void decompress(final byte[] buf, final int offset, final int size, final OutputStream out) throws
            IOException {
        if (buf == null || buf.length == 0 || size <= 0 || offset >= buf.length || out == null) {
            return;
        }
        Inflater inflater = new Inflater();
        inflater.reset();
        inflater.setInput(buf, offset, size);
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
}
