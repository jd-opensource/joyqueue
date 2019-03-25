package com.jd.journalq.network.codec;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.jd.journalq.exception.JMQCode;
import com.jd.journalq.message.BrokerMessage;
import com.jd.journalq.network.command.FetchTopicMessageAck;
import com.jd.journalq.network.command.FetchTopicMessageAckData;
import com.jd.journalq.network.command.JMQCommandType;
import com.jd.journalq.network.serializer.Serializer;
import com.jd.journalq.network.transport.codec.JMQHeader;
import com.jd.journalq.network.transport.codec.PayloadCodec;
import com.jd.journalq.network.transport.command.Type;
import io.netty.buffer.ByteBuf;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;

/**
 * FetchTopicMessageAckCodec
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/7
 */
public class FetchTopicMessageAckCodec implements PayloadCodec<JMQHeader, FetchTopicMessageAck>, Type {

    @Override
    public FetchTopicMessageAck decode(JMQHeader header, ByteBuf buffer) throws Exception {
        Map<String, FetchTopicMessageAckData> result = Maps.newHashMap();
        short topicSize = buffer.readShort();
        for (int i = 0; i < topicSize; i++) {
            String topic = Serializer.readString(buffer, Serializer.SHORT_SIZE);
            short messageSize = buffer.readShort();
            List<BrokerMessage> messages = Lists.newArrayListWithCapacity(messageSize);
            for (int j = 0; j < messageSize; j++) {
                messages.add(Serializer.readBrokerMessage(buffer));
            }
            JMQCode code = JMQCode.valueOf(buffer.readInt());
            FetchTopicMessageAckData fetchTopicMessageAckData = new FetchTopicMessageAckData(messages, code);
            result.put(topic, fetchTopicMessageAckData);
        }

        FetchTopicMessageAck fetchTopicMessageAck = new FetchTopicMessageAck();
        fetchTopicMessageAck.setData(result);
        return fetchTopicMessageAck;
    }

    @Override
    public void encode(FetchTopicMessageAck payload, ByteBuf buffer) throws Exception {
        buffer.writeShort(payload.getData().size());
        for (Map.Entry<String, FetchTopicMessageAckData> entry : payload.getData().entrySet()) {
            FetchTopicMessageAckData fetchTopicMessageAckData = entry.getValue();
            Serializer.write(entry.getKey(), buffer, Serializer.SHORT_SIZE);
            buffer.writeShort(fetchTopicMessageAckData.getBuffers().size());
            for (ByteBuffer rByteBuffer : fetchTopicMessageAckData.getBuffers()) {
                buffer.writeBytes(rByteBuffer);
            }
            buffer.writeInt(fetchTopicMessageAckData.getCode().getCode());
        }
    }

    @Override
    public int type() {
        return JMQCommandType.FETCH_TOPIC_MESSAGE_ACK.getCode();
    }
}
