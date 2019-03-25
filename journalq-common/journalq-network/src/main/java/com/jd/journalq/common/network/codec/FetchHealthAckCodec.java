package com.jd.journalq.common.network.codec;

import com.jd.journalq.common.network.command.FetchHealthAck;
import com.jd.journalq.common.network.command.JMQCommandType;
import com.jd.journalq.common.network.transport.codec.JMQHeader;
import com.jd.journalq.common.network.transport.codec.PayloadCodec;
import com.jd.journalq.common.network.transport.command.Type;
import io.netty.buffer.ByteBuf;

/**
 * FetchHealthAckCodec
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/28
 */
public class FetchHealthAckCodec implements PayloadCodec<JMQHeader, FetchHealthAck>, Type {

    @Override
    public Object decode(JMQHeader header, ByteBuf buffer) throws Exception {
        double point = buffer.readDouble();
        FetchHealthAck fetchHealthAck = new FetchHealthAck();
        fetchHealthAck.setPoint(point);
        return fetchHealthAck;
    }

    @Override
    public void encode(FetchHealthAck payload, ByteBuf buffer) throws Exception {
        buffer.writeDouble(payload.getPoint());
    }

    @Override
    public int type() {
        return JMQCommandType.FETCH_HEALTH_ACK.getCode();
    }
}