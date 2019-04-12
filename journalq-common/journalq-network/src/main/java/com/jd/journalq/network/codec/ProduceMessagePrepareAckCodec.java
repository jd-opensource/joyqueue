package com.jd.journalq.network.codec;

import com.jd.journalq.exception.JMQCode;
import com.jd.journalq.network.command.JMQCommandType;
import com.jd.journalq.network.command.ProduceMessagePrepareResponse;
import com.jd.journalq.network.serializer.Serializer;
import com.jd.journalq.network.transport.codec.JMQHeader;
import com.jd.journalq.network.transport.codec.PayloadCodec;
import com.jd.journalq.network.transport.command.Type;
import io.netty.buffer.ByteBuf;

/**
 * ProduceMessagePrepareAckCodec
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/19
 */
public class ProduceMessagePrepareAckCodec implements PayloadCodec<JMQHeader, ProduceMessagePrepareResponse>, Type {

    @Override
    public ProduceMessagePrepareResponse decode(JMQHeader header, ByteBuf buffer) throws Exception {
        ProduceMessagePrepareResponse produceMessagePrepareResponse = new ProduceMessagePrepareResponse();
        produceMessagePrepareResponse.setTxId(Serializer.readString(buffer, Serializer.SHORT_SIZE));
        produceMessagePrepareResponse.setCode(JMQCode.valueOf(buffer.readInt()));
        return produceMessagePrepareResponse;
    }

    @Override
    public void encode(ProduceMessagePrepareResponse payload, ByteBuf buffer) throws Exception {
        Serializer.write(payload.getTxId(), buffer, Serializer.SHORT_SIZE);
        buffer.writeInt(payload.getCode().getCode());
    }

    @Override
    public int type() {
        return JMQCommandType.PRODUCE_MESSAGE_PREPARE_RESPONSE.getCode();
    }
}