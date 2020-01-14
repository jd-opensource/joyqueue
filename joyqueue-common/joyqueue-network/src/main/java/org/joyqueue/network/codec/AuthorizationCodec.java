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
package org.joyqueue.network.codec;

import org.joyqueue.network.command.Authorization;
import org.joyqueue.network.command.CommandType;
import org.joyqueue.network.serializer.Serializer;
import org.joyqueue.network.session.Language;
import org.joyqueue.network.transport.codec.PayloadCodec;
import org.joyqueue.network.transport.command.Header;
import org.joyqueue.network.transport.command.Type;
import io.netty.buffer.ByteBuf;

/**
 * @author wylixiaobin
 * Date: 2018/11/28
 */
public class AuthorizationCodec implements PayloadCodec<Header, Authorization>, Type {
    @Override
    public Object decode(Header header, ByteBuf buffer) throws Exception {
        // 1字节用户长度
        return new Authorization().app(Serializer.readString(buffer, 1))
                // 1字节密码长度
                .token(Serializer.readString(buffer, 1))
                // 1字节语言
                .language(Language.valueOf(buffer.readUnsignedByte()))
                // 1字节版本长度
                .clientVersion(Serializer.readString(buffer, 1));
    }
    @Override
    public int type() {
        return CommandType.AUTHORIZATION;
    }

    @Override
    public void encode(Authorization authorization, ByteBuf buffer) throws Exception {
        Serializer.write(authorization.getApp(),buffer);
        Serializer.write(authorization.getToken(),buffer);
        buffer.writeByte(authorization.getLanguage().ordinal());
        Serializer.write(authorization.getClientVersion(),buffer);
    }
}
