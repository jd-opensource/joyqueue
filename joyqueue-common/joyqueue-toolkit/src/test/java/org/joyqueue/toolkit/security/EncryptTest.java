/**
 * Copyright 2019 The JoyQueue Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.joyqueue.toolkit.security;

import org.joyqueue.toolkit.security.Crc32;
import org.joyqueue.toolkit.security.Des;
import org.joyqueue.toolkit.security.Encrypt;
import org.joyqueue.toolkit.security.Md5;
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
