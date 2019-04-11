package com.jd.journalq.network.codec;

import com.jd.journalq.network.command.JMQCommandType;
import com.jd.journalq.network.command.ProduceMessageRollbackRequest;
import com.jd.journalq.network.serializer.Serializer;
import com.jd.journalq.network.transport.codec.JMQHeader;
import com.jd.journalq.network.transport.codec.PayloadCodec;
import com.jd.journalq.network.transport.command.Type;
import io.netty.buffer.ByteBuf;

/**
 * ProduceMessageRollbackCodec
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/19
 */
public class ProduceMessageRollbackCodec implements PayloadCodec<JMQHeader, ProduceMessageRollbackRequest>, Type {

    @Override
    public ProduceMessageRollbackRequest decode(JMQHeader header, ByteBuf buffer) throws Exception {
        ProduceMessageRollbackRequest produceMessageRollbackRequest = new ProduceMessageRollbackRequest();
        produceMessageRollbackRequest.setTopic(Serializer.readString(buffer, Serializer.SHORT_SIZE));
        produceMessageRollbackRequest.setApp(Serializer.readString(buffer, Serializer.SHORT_SIZE));
        produceMessageRollbackRequest.setTxId(Serializer.readString(buffer, Serializer.SHORT_SIZE));
        return produceMessageRollbackRequest;
    }

    @Override
    public void encode(ProduceMessageRollbackRequest payload, ByteBuf buffer) throws Exception {
        Serializer.write(payload.getTopic(), buffer, Serializer.SHORT_SIZE);
        Serializer.write(payload.getApp(), buffer, Serializer.SHORT_SIZE);
        Serializer.write(payload.getTxId(), buffer, Serializer.SHORT_SIZE);
    }

    @Override
    public int type() {
        return JMQCommandType.PRODUCE_MESSAGE_ROLLBACK.getCode();
    }
}