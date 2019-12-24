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
