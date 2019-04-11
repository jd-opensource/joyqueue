package com.jd.journalq.network.codec;

import com.jd.journalq.network.command.JMQCommandType;
import com.jd.journalq.network.command.ProduceMessageCommitRequest;
import com.jd.journalq.network.serializer.Serializer;
import com.jd.journalq.network.transport.codec.JMQHeader;
import com.jd.journalq.network.transport.codec.PayloadCodec;
import com.jd.journalq.network.transport.command.Type;
import io.netty.buffer.ByteBuf;

/**
 * ProduceMessageCommitCodec
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/19
 */
public class ProduceMessageCommitCodec implements PayloadCodec<JMQHeader, ProduceMessageCommitRequest>, Type {

    @Override
    public ProduceMessageCommitRequest decode(JMQHeader header, ByteBuf buffer) throws Exception {
        ProduceMessageCommitRequest produceMessageCommitRequest = new ProduceMessageCommitRequest();
        produceMessageCommitRequest.setTopic(Serializer.readString(buffer, Serializer.SHORT_SIZE));
        produceMessageCommitRequest.setApp(Serializer.readString(buffer, Serializer.SHORT_SIZE));
        produceMessageCommitRequest.setTxId(Serializer.readString(buffer, Serializer.SHORT_SIZE));
        return produceMessageCommitRequest;
    }

    @Override
    public void encode(ProduceMessageCommitRequest payload, ByteBuf buffer) throws Exception {
        Serializer.write(payload.getTopic(), buffer, Serializer.SHORT_SIZE);
        Serializer.write(payload.getApp(), buffer, Serializer.SHORT_SIZE);
        Serializer.write(payload.getTxId(), buffer, Serializer.SHORT_SIZE);
    }

    @Override
    public int type() {
        return JMQCommandType.PRODUCE_MESSAGE_COMMIT.getCode();
    }
}