package com.jd.journalq.toolkit.security;

import java.security.GeneralSecurityException;

/**
 * 加密
 * Created by hexiaofeng on 16-5-9.
 */
public interface Encryptor {

    /**
     * 加密
     *
     * @param source 数据
     * @param key    密匙
     * @return 加密后的字节数组
     * @throws GeneralSecurityException
     */
    byte[] encrypt(byte[] source, byte[] key) throws GeneralSecurityException;
}
