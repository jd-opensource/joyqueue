package com.jd.journalq.broker.kafka.message.converter;

import com.google.common.collect.Lists;
import com.jd.journalq.broker.kafka.message.KafkaBrokerMessage;
import com.jd.journalq.broker.kafka.message.KafkaMessageSerializer;
import com.jd.journalq.message.BrokerMessage;
import com.jd.journalq.message.SourceType;
import com.jd.journalq.toolkit.lang.Charsets;
import com.jd.journalq.toolkit.network.IpUtil;
import com.jd.journalq.toolkit.time.SystemClock;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.zip.CRC32;


/**
 * kafka消息和broker消息的转换
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/8/28
 */
public class KafkaMessageConverter {

    public static List<KafkaBrokerMessage> toKafkaBrokerMessage(String topic, int partition, List<BrokerMessage> brokerMessages) {
        List<KafkaBrokerMessage> result = Lists.newLinkedList();
        for (BrokerMessage message : brokerMessages) {
            KafkaBrokerMessage kafkaBrokerMessage = toKafkaBrokerMessage(topic, partition, message);
            result.add(kafkaBrokerMessage);
        }
        return result;
    }

    public static KafkaBrokerMessage toKafkaBrokerMessage(String topic, int partition, BrokerMessage brokerMessage) {
        KafkaBrokerMessage kafkaBrokerMessage = new KafkaBrokerMessage();
        kafkaBrokerMessage.setOffset(brokerMessage.getMsgIndexNo());
        kafkaBrokerMessage.setKey(StringUtils.isNotBlank(brokerMessage.getBusinessId()) ? brokerMessage.getBusinessId().getBytes(Charsets.UTF_8) : null);
        kafkaBrokerMessage.setValue(brokerMessage.getByteBody());
        kafkaBrokerMessage.setBatch(brokerMessage.isBatch());
        kafkaBrokerMessage.setFlag(brokerMessage.getFlag());
        KafkaMessageSerializer.readExtension(brokerMessage, kafkaBrokerMessage);
        return kafkaBrokerMessage;
    }

    public static List<BrokerMessage> toBrokerMessages(String topic, int partition, String clientId, InetSocketAddress clientAddress, List<KafkaBrokerMessage> kafkaBrokerMessages) {
        List<BrokerMessage> result = Lists.newLinkedList();
        byte[] clientAddressBytes = IpUtil.toByte(clientAddress);
        for (KafkaBrokerMessage message : kafkaBrokerMessages) {
            BrokerMessage brokerMessage = toBrokerMessage(topic, partition, clientId, clientAddressBytes, message);
            result.add(brokerMessage);
        }
        return result;
    }

    public static BrokerMessage toBrokerMessage(String topic, int partition, String clientId, InetSocketAddress clientAddress, KafkaBrokerMessage kafkaBrokerMessage) {
        return toBrokerMessage(topic, partition, clientId, IpUtil.toByte(clientAddress), kafkaBrokerMessage);
    }

    public static BrokerMessage toBrokerMessage(String topic, int partition, String clientId, byte[] clientAddress, KafkaBrokerMessage kafkaBrokerMessage) {
        BrokerMessage brokerMessage = new BrokerMessage();
        brokerMessage.setTopic(topic);
        brokerMessage.setApp(clientId);
        brokerMessage.setPartition((short) partition);
        brokerMessage.setCompressed(false);
        brokerMessage.setClientIp(clientAddress);
        brokerMessage.setBusinessId(ArrayUtils.isNotEmpty(kafkaBrokerMessage.getKey()) ? new String(kafkaBrokerMessage.getKey(), Charsets.UTF_8) : null);
        brokerMessage.setBody(kafkaBrokerMessage.getValue());
        brokerMessage.setStartTime(SystemClock.now());
        brokerMessage.setSource(SourceType.KAFKA.getValue());
        brokerMessage.setBatch(kafkaBrokerMessage.isBatch());
        brokerMessage.setFlag(kafkaBrokerMessage.getFlag());

        if (kafkaBrokerMessage.isTransaction() && ArrayUtils.isNotEmpty(kafkaBrokerMessage.getKey())) {
            brokerMessage.setTxId(new String(kafkaBrokerMessage.getKey(), Charsets.UTF_8));
        }
        KafkaMessageSerializer.writeExtension(brokerMessage, kafkaBrokerMessage);

        CRC32 crc32 = new CRC32();
        crc32.update(brokerMessage.getBody().slice());
        brokerMessage.setBodyCRC(crc32.getValue());

        return brokerMessage;
    }
}
