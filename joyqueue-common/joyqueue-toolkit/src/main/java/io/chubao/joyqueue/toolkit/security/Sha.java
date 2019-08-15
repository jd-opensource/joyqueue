package io.chubao.joyqueue.toolkit.security;

import java.security.GeneralSecurityException;
import java.security.MessageDigest;

/**
 * MD5编码
 * Created by hexiaofeng on 16-5-9.
 */
public class Sha implements Encryptor {

    public static final Sha INSTANCE = new Sha();

    @Override
    public byte[] encrypt(byte[] source, final byte[] key) throws GeneralSecurityException {
        // 获得SHA摘要算法的 MessageDigest 对象
        MessageDigest md = MessageDigest.getInstance("SHA");
        md.reset();
        // 使用指定的字节更新摘要
        md.update(source);
        // 获得密文
        return md.digest();
    }
}
