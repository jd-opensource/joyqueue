package com.jd.journalq.server.retry.remote.command.codec;

import com.jd.journalq.network.command.BooleanAck;
import com.jd.journalq.network.command.CommandType;
import com.jd.journalq.network.transport.codec.JMQHeader;
import com.jd.journalq.network.transport.codec.PayloadCodec;
import com.jd.journalq.network.transport.command.Type;
import io.netty.buffer.ByteBuf;

/**
 * Created by chengzhiliang on 2018/10/12.
 */
public class BooleanAckCodec implements PayloadCodec<JMQHeader, BooleanAck>, Type {

    @Override
    public Object decode(JMQHeader header, ByteBuf buffer) throws Exception {
        // 布尔应答不解析消息体
        return null;
    }

    @Override
    public void encode(BooleanAck payload, ByteBuf buffer) throws Exception {
        // 布尔应答编码消息体
    }

    @Override
    public int type() {
        return CommandType.BOOLEAN_ACK;
    }
}
