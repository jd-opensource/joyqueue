package com.jd.journalq.client.internal.common.compress;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * CompressUtils
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
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