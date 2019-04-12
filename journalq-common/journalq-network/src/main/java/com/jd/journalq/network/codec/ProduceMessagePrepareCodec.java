package com.jd.journalq.network.codec;

import com.jd.journalq.network.command.JMQCommandType;
import com.jd.journalq.network.command.ProduceMessagePrepareRequest;
import com.jd.journalq.network.serializer.Serializer;
import com.jd.journalq.network.transport.codec.JMQHeader;
import com.jd.journalq.network.transport.codec.PayloadCodec;
import com.jd.journalq.network.transport.command.Type;
import io.netty.buffer.ByteBuf;

/**
 * ProduceMessagePrepareCodec
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/19
 */
public class ProduceMessagePrepareCodec implements PayloadCodec<JMQHeader, ProduceMessagePrepareRequest>, Type {

    @Override
    public ProduceMessagePrepareRequest decode(JMQHeader header, ByteBuf buffer) throws Exception {
        ProduceMessagePrepareRequest produceMessagePrepareRequest = new ProduceMessagePrepareRequest();
        produceMessagePrepareRequest.setTopic(Serializer.readString(buffer, Serializer.SHORT_SIZE));
        produceMessagePrepareRequest.setApp(Serializer.readString(buffer, Serializer.SHORT_SIZE));
        produceMessagePrepareRequest.setSequence(buffer.readLong());
        produceMessagePrepareRequest.setTransactionId(Serializer.readString(buffer, Serializer.SHORT_SIZE));
        produceMessagePrepareRequest.setTimeout(buffer.readInt());
        return produceMessagePrepareRequest;
    }

    @Override
    public void encode(ProduceMessagePrepareRequest payload, ByteBuf buffer) throws Exception {
        Serializer.write(payload.getTopic(), buffer, Serializer.SHORT_SIZE);
        Serializer.write(payload.getApp(), buffer, Serializer.SHORT_SIZE);
        buffer.writeLong(payload.getSequence());
        Serializer.write(payload.getTransactionId(), buffer, Serializer.SHORT_SIZE);
        buffer.writeInt(payload.getTimeout());
    }

    @Override
    public int type() {
        return JMQCommandType.PRODUCE_MESSAGE_PREPARE_REQUEST.getCode();
    }
}