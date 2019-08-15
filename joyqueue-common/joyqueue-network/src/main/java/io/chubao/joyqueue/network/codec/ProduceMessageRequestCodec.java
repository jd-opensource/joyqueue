package io.chubao.joyqueue.network.codec;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.chubao.joyqueue.domain.QosLevel;
import io.chubao.joyqueue.message.BrokerMessage;
import io.chubao.joyqueue.network.command.JoyQueueCommandType;
import io.chubao.joyqueue.network.command.ProduceMessageRequest;
import io.chubao.joyqueue.network.command.ProduceMessageData;
import io.chubao.joyqueue.network.serializer.Serializer;
import io.chubao.joyqueue.network.transport.codec.JoyQueueHeader;
import io.chubao.joyqueue.network.transport.codec.PayloadCodec;
import io.chubao.joyqueue.network.transport.command.Type;
import io.netty.buffer.ByteBuf;

import java.util.List;
import java.util.Map;

/**
 * ProduceMessageRequestCodec
 *
 * author: gaohaoxiang
 * date: 2018/12/18
 */
public class ProduceMessageRequestCodec implements PayloadCodec<JoyQueueHeader, ProduceMessageRequest>, Type {

    @Override
    public ProduceMessageRequest decode(JoyQueueHeader header, ByteBuf buffer) throws Exception {
        short dataSize = buffer.readShort();
        Map<String, ProduceMessageData> data = Maps.newHashMapWithExpectedSize(dataSize);
        for (int i = 0; i < dataSize; i++) {
            String topic = Serializer.readString(buffer, Serializer.SHORT_SIZE);
            String txId = Serializer.readString(buffer, Serializer.SHORT_SIZE);
            int timeout = buffer.readInt();
            QosLevel qosLevel = QosLevel.valueOf(buffer.readByte());

            ProduceMessageData produceMessageData = new ProduceMessageData();
            produceMessageData.setTxId(txId);
            produceMessageData.setTimeout(timeout);
            produceMessageData.setQosLevel(qosLevel);

            short messageSize = buffer.readShort();
            List<BrokerMessage> messages = Lists.newArrayListWithCapacity(messageSize);
            for (int j = 0; j < messageSize; j++) {
                BrokerMessage brokerMessage = Serializer.readBrokerMessage(buffer);
                brokerMessage.setTopic(topic);
                brokerMessage.setTxId(txId);
                messages.add(brokerMessage);
            }

            produceMessageData.setMessages(messages);
            data.put(topic, produceMessageData);
        }

        ProduceMessageRequest produceMessageRequest = new ProduceMessageRequest();
        produceMessageRequest.setApp(Serializer.readString(buffer, Serializer.SHORT_SIZE));
        produceMessageRequest.setData(data);
        return produceMessageRequest;
    }

    @Override
    public void encode(ProduceMessageRequest payload, ByteBuf buffer) throws Exception {
        buffer.writeShort(payload.getData().size());
        for (Map.Entry<String, ProduceMessageData> entry : payload.getData().entrySet()) {
            ProduceMessageData data = entry.getValue();
            Serializer.write(entry.getKey(), buffer, Serializer.SHORT_SIZE);
            Serializer.write(data.getTxId(), buffer, Serializer.SHORT_SIZE);
            buffer.writeInt(data.getTimeout());
            buffer.writeByte(data.getQosLevel().value());
            buffer.writeShort(data.getMessages().size());
            for (BrokerMessage message : data.getMessages()) {
                Serializer.writeBrokerMessage(message, buffer);
            }
        }
        Serializer.write(payload.getApp(), buffer, Serializer.SHORT_SIZE);
    }

    @Override
    public int type() {
        return JoyQueueCommandType.PRODUCE_MESSAGE_REQUEST.getCode();
    }
}