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

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import java.security.GeneralSecurityException;
import java.security.Key;

/**
 * MD5编码
 * Created by hexiaofeng on 16-5-9.
 */
public class Des implements Encryptor, Decryptor {

    protected static final String KEY_ALGORITHM = "DES";
    protected static final String CIPHER_ALGORITHM = "DES/ECB/PKCS5Padding";
    public static final Des INSTANCE = new Des();

    @Override
    public byte[] encrypt(final byte[] source, final byte[] key) throws GeneralSecurityException {
        //还原密钥
        Key k = toDESKey(key);
        //实例化
        Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
        //初始化，设置为加密模式
        cipher.init(Cipher.ENCRYPT_MODE, k);
        //执行操作
        return cipher.doFinal(source);
    }

    @Override
    public byte[] decrypt(final byte[] source, final byte[] key) throws GeneralSecurityException {
        //欢迎密钥
        Key k = toDESKey(key);
        //实例化
        Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
        //初始化，设置为解密模式
        cipher.init(Cipher.DECRYPT_MODE, k);
        //执行操作
        return cipher.doFinal(source);
    }

    /**
     * 转换密钥
     *
     * @param key 二进制密钥
     * @return Key 密钥
     */
    protected static Key toDESKey(final byte[] key) throws GeneralSecurityException {
        //实例化Des密钥
        DESKeySpec dks = new DESKeySpec(key);
        //实例化密钥工厂
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(KEY_ALGORITHM);
        //生成密钥
        SecretKey secretKey = keyFactory.generateSecret(dks);
        return secretKey;
    }

}
