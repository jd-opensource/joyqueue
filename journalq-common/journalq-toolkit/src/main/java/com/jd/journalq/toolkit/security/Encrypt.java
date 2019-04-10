package com.jd.journalq.toolkit.security;

import com.jd.journalq.toolkit.lang.Charsets;

import java.nio.charset.Charset;
import java.security.GeneralSecurityException;

/**
 * 加密工具
 */
public class Encrypt {

    public static final String DEFAULT_KEY = "www.jd.com";

    /**
     * 加密
     *
     * @param source    数据源
     * @param key       密匙
     * @param encryptor 加密器
     * @return 加密后的数据
     */
    public static String encrypt(final String source, final String key, final Encryptor encryptor) throws
            GeneralSecurityException {
        return encrypt(source, key, encryptor, Charsets.UTF_8);
    }

    /**
     * 加密
     *
     * @param source    数据源
     * @param key       密匙
     * @param encryptor 加密器
     * @param charset   字符集
     * @return 加密后的数据
     */
    public static String encrypt(final String source, final String key, final Encryptor encryptor,
            final Charset charset) throws GeneralSecurityException {
        if (source == null) {
            return "";
        }
        if (encryptor == null) {
            return source;
        }
        byte[] sources;
        byte[] keys = null;
        if (charset == null) {
            sources = source.getBytes();
            if (key != null) {
                keys = key.getBytes();
            }
        } else {
            sources = source.getBytes(charset);
            if (key != null) {
                keys = key.getBytes(charset);
            }
        }
        return Hex.encode(encryptor.encrypt(sources, keys));
    }

    /**
     * 解密
     *
     * @param source    数据源
     * @param key       密匙
     * @param decryptor 解密器
     * @return 解密后的数据
     */
    public static String decrypt(final String source, final String key, final Decryptor decryptor) throws
            GeneralSecurityException {
        return decrypt(source, key, decryptor, Charsets.UTF_8);
    }

    /**
     * 解密
     *
     * @param source    数据源
     * @param key       密匙
     * @param decryptor 解密器
     * @param charset   字符集
     * @return 解密后的数据
     */
    public static String decrypt(final String source, final String key, final Decryptor decryptor,
            final Charset charset) throws GeneralSecurityException {
        if (source == null) {
            return "";
        }
        if (decryptor == null) {
            return source;
        }
        byte[] sources;
        byte[] keys = null;
        if (charset == null) {
            sources = Hex.decode(source);
            if (key != null) {
                keys = key.getBytes();
            }
            return new String(decryptor.decrypt(sources, keys));
        } else {
            sources = Hex.decode(source);
            if (key != null) {
                keys = key.getBytes(charset);
            }
            return new String(decryptor.decrypt(sources, keys), charset);
        }
    }
}
