package com.jd.journalq.toolkit.security;

import org.junit.Assert;
import org.junit.Test;

import java.security.GeneralSecurityException;

/**
 * Created by hexiaofeng on 16-5-9.
 */
public class EncryptTest {

    @Test
    public void testMd5() throws GeneralSecurityException {
        String source = "123456789";
        String encode = Encrypt.encrypt(source, null, Md5.INSTANCE);
        Assert.assertEquals(encode, "25F9E794323B453885F5181F1B624D0B");
    }

    @Test
    public void testDes() throws GeneralSecurityException {
        String source = "123456789";
        String encode = Encrypt.encrypt(source, "1234567.xxxx", Des.INSTANCE);
        String decode = Encrypt.decrypt(encode, "1234567.xxxx", Des.INSTANCE);
        Assert.assertEquals(source, decode);
    }

    @Test
    public void testCRC32() {
        String source = "123456789";
        java.util.zip.CRC32 c1 = new java.util.zip.CRC32();
        c1.update(source.getBytes());
        long v1 = c1.getValue();
        Crc32 c2 = new Crc32();
        c2.update(source.getBytes());
        long v2 = c2.getValue();
        Assert.assertEquals(v1, v2);
    }
}
