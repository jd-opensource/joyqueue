package com.jd.journalq.network.codec;

import com.jd.journalq.network.command.JMQCommandType;
import com.jd.journalq.network.command.RemoveConnection;
import com.jd.journalq.network.transport.codec.JMQHeader;
import com.jd.journalq.network.transport.codec.PayloadCodec;
import com.jd.journalq.network.transport.command.Type;
import io.netty.buffer.ByteBuf;

/**
 * RemoveConnectionCodec
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/11/30
 */
public class RemoveConnectionCodec implements PayloadCodec<JMQHeader, RemoveConnection>, Type {

    @Override
    public RemoveConnection decode(JMQHeader header, ByteBuf buffer) throws Exception {
        return new RemoveConnection();
    }

    @Override
    public void encode(RemoveConnection payload, ByteBuf buffer) throws Exception {
    }

    @Override
    public int type() {
        return JMQCommandType.REMOVE_CONNECTION.getCode();
    }
}