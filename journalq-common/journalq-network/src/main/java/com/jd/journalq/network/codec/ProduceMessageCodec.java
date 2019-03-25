package com.jd.journalq.network.codec;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.jd.journalq.domain.QosLevel;
import com.jd.journalq.message.BrokerMessage;
import com.jd.journalq.network.command.JMQCommandType;
import com.jd.journalq.network.command.ProduceMessage;
import com.jd.journalq.network.command.ProduceMessageData;
import com.jd.journalq.network.serializer.Serializer;
import com.jd.journalq.network.transport.codec.JMQHeader;
import com.jd.journalq.network.transport.codec.PayloadCodec;
import com.jd.journalq.network.transport.command.Type;
import io.netty.buffer.ByteBuf;

import java.util.List;
import java.util.Map;

/**
 * ProduceMessageCodec
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/18
 */
public class ProduceMessageCodec implements PayloadCodec<JMQHeader, ProduceMessage>, Type {

    @Override
    public ProduceMessage decode(JMQHeader header, ByteBuf buffer) throws Exception {
        short dataSize = buffer.readShort();
        Map<String, ProduceMessageData> data = Maps.newHashMap();
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

        ProduceMessage produceMessage = new ProduceMessage();
        produceMessage.setApp(Serializer.readString(buffer, Serializer.SHORT_SIZE));
        produceMessage.setData(data);
        return produceMessage;
    }

    @Override
    public void encode(ProduceMessage payload, ByteBuf buffer) throws Exception {
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
        return JMQCommandType.PRODUCE_MESSAGE.getCode();
    }
}