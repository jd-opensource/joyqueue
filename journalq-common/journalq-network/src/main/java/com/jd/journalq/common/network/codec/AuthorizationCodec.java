package com.jd.journalq.common.network.codec;

import com.jd.journalq.common.network.command.Authorization;
import com.jd.journalq.common.network.command.CommandType;
import com.jd.journalq.common.network.serializer.Serializer;
import com.jd.journalq.common.network.session.Language;
import com.jd.journalq.common.network.transport.codec.PayloadCodec;
import com.jd.journalq.common.network.transport.command.Header;
import com.jd.journalq.common.network.transport.command.Type;
import io.netty.buffer.ByteBuf;

/**
 * @author wylixiaobin
 * Date: 2018/11/28
 */
public class AuthorizationCodec implements PayloadCodec<Header,Authorization>,Type {
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
