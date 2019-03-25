package com.jd.journalq.toolkit.io;

import com.jd.journalq.toolkit.lang.Charsets;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;

/**
 * 压缩工具类
 * Created by hexiaofeng on 16-5-6.
 */
public abstract class Compressors {

    /**
     * 压缩
     *
     * @param src        字符串
     * @param compressor 压缩器
     * @throws java.io.IOException
     */
    public static byte[] compress(final String src, final Compressor compressor) throws IOException {
        return compress(src, Charsets.UTF_8, compressor);
    }

    /**
     * 压缩
     *
     * @param src        字符串
     * @param charset    字符串
     * @param compressor 压缩器
     * @throws java.io.IOException
     */
    public static byte[] compress(final String src, final Charset charset, final Compressor compressor) throws
            IOException {
        if (src == null) {
            return null;
        }
        if (charset == null) {
            return compress(src.getBytes(), compressor);
        }
        return compress(src.getBytes(charset), compressor);
    }

    /**
     * 压缩
     *
     * @param buf        缓冲器
     * @param compressor 压缩器
     * @return 压缩后的数据
     * @throws IOException
     */
    public static byte[] compress(final byte[] buf, final Compressor compressor) throws IOException {
        if (buf == null) {
            return null;
        }
        return compress(buf, 0, buf.length, compressor);
    }

    /**
     * 压缩
     *
     * @param buf        缓冲器
     * @param offset     偏移量
     * @param size       长度
     * @param compressor 压缩器
     * @return 压缩后的数据
     * @throws IOException
     */
    public static byte[] compress(final byte[] buf, final int offset, final int size,
            final Compressor compressor) throws IOException {
        if (buf == null) {
            return null;
        }
        ByteArrayOutputStream bos = new ByteArrayOutputStream(size);
        try {
            compressor.compress(buf, offset, size, bos);
            return bos.toByteArray();
        } finally {
            bos.close();
        }
    }

    /**
     * 解压缩
     *
     * @param buf        缓冲器
     * @param compressor 压缩器
     * @throws java.io.IOException
     */
    public static byte[] decompress(final byte[] buf, final Compressor compressor) throws IOException {
        if (buf == null) {
            return null;
        }
        return decompress(buf, 0, buf.length, compressor);
    }

    /**
     * 解压缩
     *
     * @param buf        缓冲器
     * @param offset     偏移量
     * @param size       长度
     * @param compressor 压缩器
     * @throws java.io.IOException
     */
    public static byte[] decompress(final byte[] buf, final int offset, final int size,
            final Compressor compressor) throws IOException {
        if (buf == null || buf.length == 0 || size <= 0 || offset >= buf.length) {
            return null;
        }
        ByteArrayOutputStream bos = new ByteArrayOutputStream(size * 4);
        try {
            compressor.decompress(buf, offset, size, bos);
            return bos.toByteArray();
        } finally {
            bos.close();
        }
    }

    /**
     * 解压缩
     *
     * @param buf        缓冲器
     * @param charset    字符集
     * @param compressor 压缩器
     * @throws java.io.IOException
     */
    public static String decompress(final byte[] buf, final Charset charset, final Compressor compressor) throws
            IOException {
        if (buf == null || buf.length == 0) {
            return null;
        }
        return decompress(buf, 0, buf.length, charset, compressor);
    }

    /**
     * 解压缩
     *
     * @param buf        缓冲器
     * @param offset     偏移量
     * @param size       长度
     * @param charset    字符集
     * @param compressor 压缩器
     * @throws java.io.IOException
     */
    public static String decompress(final byte[] buf, final int offset, final int size, final Charset charset,
            final Compressor compressor) throws IOException {
        if (buf == null || buf.length == 0 || size <= 0 || offset >= buf.length) {
            return null;
        }
        ByteArrayOutputStream bos = new ByteArrayOutputStream(size * 4);
        try {
            compressor.decompress(buf, offset, size, bos);
            return bos.toString(charset.toString());
        } finally {
            bos.close();
        }
    }
}
