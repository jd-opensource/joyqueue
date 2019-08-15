package io.chubao.joyqueue.toolkit.security;

import java.security.GeneralSecurityException;
import java.security.MessageDigest;

/**
 * MD5编码
 * Created by hexiaofeng on 16-5-9.
 */
public class Md5 implements Encryptor {

    public static final Md5 INSTANCE = new Md5();

    @Override
    public byte[] encrypt(final byte[] source, final byte[] key) throws GeneralSecurityException {
        // 获得MD5摘要算法的 MessageDigest 对象
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.reset();
        // 使用指定的字节更新摘要
        md.update(source);
        // 获得密文
        return md.digest();
    }

}
