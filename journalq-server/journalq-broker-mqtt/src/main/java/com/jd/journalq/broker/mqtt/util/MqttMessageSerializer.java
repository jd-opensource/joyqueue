package com.jd.journalq.broker.mqtt.util;

import com.jd.journalq.message.BrokerMessage;
import com.jd.journalq.message.SourceType;
import com.jd.journalq.toolkit.network.IpUtil;
import com.jd.journalq.toolkit.time.SystemClock;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.*;

import java.util.zip.CRC32;

/**
 * @author majun8
 */
public class MqttMessageSerializer {

    private static final int EXTENSION_QOS_LENGTH = 4;

    public static BrokerMessage convertToBrokerMsg(Channel client, MqttPublishMessage mqttMessage) {
        BrokerMessage brokerMessage = new BrokerMessage();

        long now = SystemClock.now();
        final String clientId = NettyAttrManager.getAttrClientId(client);
        brokerMessage.setApp(clientId);
        brokerMessage.setTopic(mqttMessage.variableHeader().topicName());
        brokerMessage.setCompressed(false);
        brokerMessage.setClientIp(IpUtil.toAddress(client.remoteAddress()).getBytes());
        brokerMessage.setStartTime(now);
        brokerMessage.setSource(SourceType.MQTT.getValue());
        brokerMessage.setBusinessId(Integer.toString(mqttMessage.variableHeader().packetId()));

        ByteBuf payload = mqttMessage.payload();
        byte[] body = new byte[payload.readableBytes()];
        int index = payload.readerIndex();
        payload.readBytes(body);
        payload.readerIndex(index);
        brokerMessage.setBody(body);
        writeExtension(mqttMessage.fixedHeader().qosLevel(), brokerMessage);

        CRC32 crc32 = new CRC32();
        crc32.update(brokerMessage.getBody().slice());
        brokerMessage.setBodyCRC(crc32.getValue());

        return brokerMessage;
    }

    public static void writeExtension(MqttQoS qos, BrokerMessage brokerMessage) {
        byte[] extension = new byte[] {
                (byte) ((qos.value() >> 24) & 0xFF),
                (byte) ((qos.value() >> 16) & 0xFF),
                (byte) ((qos.value() >> 8) & 0xFF),
                (byte) (qos.value() & 0xFF)
        };
        brokerMessage.setExtension(extension);
    }

    public static int readExtension(BrokerMessage brokerMessage) {
        byte[] extension = brokerMessage.getExtension();
        if (extension != null && extension.length == 4) {
            return extension[3] & 0xFF |
                    (extension[2] & 0xFF) << 8 |
                    (extension[1] & 0xFF) << 16 |
                    (extension[0] & 0xFF) << 24;
        } else {
            return 0;
        }
    }

    public static int getLowerQos(int qos1,int qos2){
        if(qos1 < qos2){
            return qos1;
        }
        return qos2;
    }
}
