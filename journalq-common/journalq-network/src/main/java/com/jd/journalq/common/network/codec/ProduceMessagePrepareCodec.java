package com.jd.journalq.common.network.codec;

import com.jd.journalq.common.network.command.JMQCommandType;
import com.jd.journalq.common.network.command.ProduceMessagePrepare;
import com.jd.journalq.common.network.serializer.Serializer;
import com.jd.journalq.common.network.transport.codec.JMQHeader;
import com.jd.journalq.common.network.transport.codec.PayloadCodec;
import com.jd.journalq.common.network.transport.command.Type;
import io.netty.buffer.ByteBuf;

/**
 * ProduceMessagePrepareCodec
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/19
 */
public class ProduceMessagePrepareCodec implements PayloadCodec<JMQHeader, ProduceMessagePrepare>, Type {

    @Override
    public ProduceMessagePrepare decode(JMQHeader header, ByteBuf buffer) throws Exception {
        ProduceMessagePrepare produceMessagePrepare = new ProduceMessagePrepare();
        produceMessagePrepare.setTopic(Serializer.readString(buffer, Serializer.SHORT_SIZE));
        produceMessagePrepare.setApp(Serializer.readString(buffer, Serializer.SHORT_SIZE));
        produceMessagePrepare.setSequence(buffer.readLong());
        produceMessagePrepare.setTransactionId(Serializer.readString(buffer, Serializer.SHORT_SIZE));
        produceMessagePrepare.setTimeout(buffer.readInt());
        return produceMessagePrepare;
    }

    @Override
    public void encode(ProduceMessagePrepare payload, ByteBuf buffer) throws Exception {
        Serializer.write(payload.getTopic(), buffer, Serializer.SHORT_SIZE);
        Serializer.write(payload.getApp(), buffer, Serializer.SHORT_SIZE);
        buffer.writeLong(payload.getSequence());
        Serializer.write(payload.getTransactionId(), buffer, Serializer.SHORT_SIZE);
        buffer.writeInt(payload.getTimeout());
    }

    @Override
    public int type() {
        return JMQCommandType.PRODUCE_MESSAGE_PREPARE.getCode();
    }
}