package com.jd.journalq.common.network.codec;

import com.jd.journalq.common.network.command.FetchProduceFeedback;
import com.jd.journalq.common.network.command.JMQCommandType;
import com.jd.journalq.common.network.command.TxStatus;
import com.jd.journalq.common.network.serializer.Serializer;
import com.jd.journalq.common.network.transport.codec.JMQHeader;
import com.jd.journalq.common.network.transport.codec.PayloadCodec;
import com.jd.journalq.common.network.transport.command.Type;
import io.netty.buffer.ByteBuf;

/**
 * FetchProduceFeedbackCodec
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/19
 */
public class FetchProduceFeedbackCodec implements PayloadCodec<JMQHeader, FetchProduceFeedback>, Type {

    @Override
    public FetchProduceFeedback decode(JMQHeader header, ByteBuf buffer) throws Exception {
        FetchProduceFeedback fetchProduceFeedback = new FetchProduceFeedback();
        fetchProduceFeedback.setApp(Serializer.readString(buffer, Serializer.SHORT_SIZE));
        fetchProduceFeedback.setTopic(Serializer.readString(buffer, Serializer.SHORT_SIZE));
        fetchProduceFeedback.setStatus(TxStatus.valueOf(buffer.readByte()));
        fetchProduceFeedback.setLongPollTimeout(buffer.readInt());
        return fetchProduceFeedback;
    }

    @Override
    public void encode(FetchProduceFeedback payload, ByteBuf buffer) throws Exception {
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