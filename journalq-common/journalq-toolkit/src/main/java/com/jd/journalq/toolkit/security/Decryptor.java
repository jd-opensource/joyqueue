package com.jd.journalq.toolkit.security;

import java.security.GeneralSecurityException;

/**
 * 解密
 * Created by hexiaofeng on 16-5-9.
 */
public interface Decryptor {

    /**
     * 解密
     *
     * @param source 数据
     * @param key    密匙
     * @return 解密后的字节数组
     * @throws GeneralSecurityException
     */
    byte[] decrypt(byte[] source, byte[] key) throws GeneralSecurityException;
}
