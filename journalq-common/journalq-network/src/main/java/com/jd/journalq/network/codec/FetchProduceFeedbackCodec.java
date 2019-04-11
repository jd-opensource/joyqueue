package com.jd.journalq.network.codec;

import com.jd.journalq.network.command.FetchProduceFeedbackRequest;
import com.jd.journalq.network.command.JMQCommandType;
import com.jd.journalq.network.command.TxStatus;
import com.jd.journalq.network.serializer.Serializer;
import com.jd.journalq.network.transport.codec.JMQHeader;
import com.jd.journalq.network.transport.codec.PayloadCodec;
import com.jd.journalq.network.transport.command.Type;
import io.netty.buffer.ByteBuf;

/**
 * FetchProduceFeedbackCodec
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/19
 */
public class FetchProduceFeedbackCodec implements PayloadCodec<JMQHeader, FetchProduceFeedbackRequest>, Type {

    @Override
    public FetchProduceFeedbackRequest decode(JMQHeader header, ByteBuf buffer) throws Exception {
        FetchProduceFeedbackRequest fetchProduceFeedbackRequest = new FetchProduceFeedbackRequest();
        fetchProduceFeedbackRequest.setApp(Serializer.readString(buffer, Serializer.SHORT_SIZE));
        fetchProduceFeedbackRequest.setTopic(Serializer.readString(buffer, Serializer.SHORT_SIZE));
        fetchProduceFeedbackRequest.setStatus(TxStatus.valueOf(buffer.readByte()));
        fetchProduceFeedbackRequest.setLongPollTimeout(buffer.readInt());
        return fetchProduceFeedbackRequest;
    }

    @Override
    public void encode(FetchProduceFeedbackRequest payload, ByteBuf buffer) throws Exception {
        Serializer.write(payload.getApp(), buffer, Serializer.SHORT_SIZE);
        Serializer.write(payload.getTopic(), buffer, Serializer.SHORT_SIZE);
        buffer.writeByte(payload.getStatus().getType());
        buffer.writeInt(payload.getLongPollTimeout());
    }

    @Override
    public int type() {
        return JMQCommandType.FETCH_PRODUCE_FEEDBACK.getCode();
    }
}