package com.jd.journalq.network.codec;

import com.jd.journalq.network.command.FetchHealthResponse;
import com.jd.journalq.network.command.JMQCommandType;
import com.jd.journalq.network.transport.codec.JMQHeader;
import com.jd.journalq.network.transport.codec.PayloadCodec;
import com.jd.journalq.network.transport.command.Type;
import io.netty.buffer.ByteBuf;

/**
 * FetchHealthAckCodec
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/28
 */
public class FetchHealthAckCodec implements PayloadCodec<JMQHeader, FetchHealthResponse>, Type {

    @Override
    public Object decode(JMQHeader header, ByteBuf buffer) throws Exception {
        double point = buffer.readDouble();
        FetchHealthResponse fetchHealthResponse = new FetchHealthResponse();
        fetchHealthResponse.setPoint(point);
        return fetchHealthResponse;
    }

    @Override
    public void encode(FetchHealthResponse payload, ByteBuf buffer) throws Exception {
        buffer.writeDouble(payload.getPoint());
    }

    @Override
    public int type() {
        return JMQCommandType.FETCH_HEALTH_ACK.getCode();
    }
}