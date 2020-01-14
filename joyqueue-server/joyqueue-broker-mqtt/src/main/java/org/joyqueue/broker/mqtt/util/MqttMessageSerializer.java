/**
 * Copyright 2019 The JoyQueue Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.joyqueue.broker.mqtt.util;

import org.joyqueue.message.BrokerMessage;
import org.joyqueue.message.SourceType;
import org.joyqueue.toolkit.network.IpUtil;
import org.joyqueue.toolkit.time.SystemClock;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.MqttPublishMessage;
import io.netty.handler.codec.mqtt.MqttQoS;

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
