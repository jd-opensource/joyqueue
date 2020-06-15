package com.jd.joyqueue.broker.jmq2.network.codec;

import com.jd.joyqueue.broker.jmq2.JMQ2CommandType;
import com.jd.joyqueue.broker.jmq2.command.AddConnection;
import com.jd.joyqueue.broker.jmq2.network.JMQ2PayloadCodec;
import com.jd.joyqueue.broker.jmq2.util.Serializer;
import org.joyqueue.network.session.ConnectionId;
import org.joyqueue.network.session.Language;
import org.joyqueue.network.transport.command.Header;
import org.joyqueue.network.transport.command.Payload;
import org.joyqueue.network.transport.command.Type;
import io.netty.buffer.ByteBuf;

/**
 * 添加连接编解码器
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/8/21
 */
public class AddConnectionCodec implements JMQ2PayloadCodec, Type {

    @Override
    public Object decode(Header header, ByteBuf buffer) throws Exception {
        AddConnection payload = new AddConnection();
        // 1字节链接ID长度
        payload.setConnectionId(new ConnectionId(Serializer.readString(buffer, 1)));
        // 1字节用户长度
        payload.setUser(Serializer.readString(buffer, 1));
        // 1字节密码长度
        payload.setPassword(Serializer.readString(buffer, 1));
        // 1字节应用长度
        payload.setApp(Serializer.readString(buffer, 1));
        // 1字节语言
        payload.setLanguage(Language.valueOf(buffer.readUnsignedByte()));
        // 1字节版本长度
        payload.setClientVersion(Serializer.readString(buffer, 1));
        return payload;
    }

    @Override
    public void encode(Payload payload, ByteBuf buffer) {

    }

    @Override
    public int type() {
        return JMQ2CommandType.ADD_CONNECTION.getCode();
    }
}