package io.chubao.joyqueue.network.codec;

import com.google.common.collect.Lists;
import io.chubao.joyqueue.exception.JoyQueueCode;
import io.chubao.joyqueue.network.command.FetchProduceFeedbackAckData;
import io.chubao.joyqueue.network.command.FetchProduceFeedbackResponse;
import io.chubao.joyqueue.network.command.JoyQueueCommandType;
import io.chubao.joyqueue.network.serializer.Serializer;
import io.chubao.joyqueue.network.transport.codec.JoyQueueHeader;
import io.chubao.joyqueue.network.transport.codec.PayloadCodec;
import io.chubao.joyqueue.network.transport.command.Type;
import io.netty.buffer.ByteBuf;

import java.util.List;

/**
 * FetchProduceFeedbackResponseCodec
 *
 * author: gaohaoxiang
 * date: 2018/12/19
 */
public class FetchProduceFeedbackResponseCodec implements PayloadCodec<JoyQueueHeader, FetchProduceFeedbackResponse>, Type {

    @Override
    public FetchProduceFeedbackResponse decode(JoyQueueHeader header, ByteBuf buffer) throws Exception {
        short dataSize = buffer.readShort();
        List<FetchProduceFeedbackAckData> data = Lists.newArrayListWithCapacity(dataSize);
        for (int i = 0; i < dataSize; i++) {
            String topic = Serializer.readString(buffer, Serializer.SHORT_SIZE);
            String txId = Serializer.readString(buffer, Serializer.SHORT_SIZE);
            String transactionId = Serializer.readString(buffer, Serializer.SHORT_SIZE);
            data.add(new FetchProduceFeedbackAckData(topic, txId, transactionId));
        }

        FetchProduceFeedbackResponse fetchProduceFeedbackResponse = new FetchProduceFeedbackResponse();
        fetchProduceFeedbackResponse.setData(data);
        fetchProduceFeedbackResponse.setCode(JoyQueueCode.valueOf(buffer.readInt()));
        return fetchProduceFeedbackResponse;
    }

    @Override
    public void encode(FetchProduceFeedbackResponse payload, ByteBuf buffer) throws Exception {
        buffer.writeShort(payload.getData().size());
        for (FetchProduceFeedbackAckData data : payload.getData()) {
            Serializer.write(data.getTopic(), buffer, Serializer.SHORT_SIZE);
            Serializer.write(data.getTxId(), buffer, Serializer.SHORT_SIZE);
            Serializer.write(data.getTransactionId(), buffer, Serializer.SHORT_SIZE);
        }
        buffer.writeInt(payload.getCode().getCode());
    }

    @Override
    public int type() {
        return JoyQueueCommandType.FETCH_PRODUCE_FEEDBACK_RESPONSE.getCode();
    }
}