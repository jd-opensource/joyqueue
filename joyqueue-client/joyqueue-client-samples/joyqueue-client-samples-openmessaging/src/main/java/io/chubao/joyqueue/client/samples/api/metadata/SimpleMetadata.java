package io.chubao.joyqueue.client.samples.api.metadata;

import io.openmessaging.KeyValue;
import io.openmessaging.MessagingAccessPoint;
import io.openmessaging.OMS;
import io.openmessaging.OMSBuiltinKeys;
import io.openmessaging.extension.QueueMetaData;
import io.openmessaging.message.Message;
import io.openmessaging.producer.Producer;
import io.openmessaging.producer.SendResult;

/**
 * SimpleMetadata
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/4/8
 */
public class SimpleMetadata {

    public static void main(String[] args) {
        KeyValue keyValue = OMS.newKeyValue();
        keyValue.put(OMSBuiltinKeys.ACCOUNT_KEY, "test_token");

        MessagingAccessPoint messagingAccessPoint = OMS.getMessagingAccessPoint("oms:joyqueue://test_app@127.0.0.1:50088/UNKNOWN", keyValue);

        Producer producer = messagingAccessPoint.createProducer();
        producer.start();

        QueueMetaData queueMetaData = producer.getQueueMetaData("test_topic_0");
        for (QueueMetaData.Partition partition : queueMetaData.partitions()) {
            System.out.println(String.format("partition: %s, partitionHost: %s", partition.partitionId(), partition.partitonHost()));
        }

        Message message = producer.createMessage("test_topic_0", "body".getBytes());

        SendResult sendResult = producer.send(message);
        System.out.println(String.format("messageId: %s", sendResult.messageId()));
    }
}