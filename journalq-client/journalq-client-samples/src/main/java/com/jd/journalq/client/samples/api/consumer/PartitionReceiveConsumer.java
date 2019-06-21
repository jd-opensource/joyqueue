package com.jd.journalq.client.samples.api.consumer;

import com.jd.journalq.toolkit.network.IpUtil;
import io.openmessaging.KeyValue;
import io.openmessaging.MessagingAccessPoint;
import io.openmessaging.OMS;
import io.openmessaging.OMSBuiltinKeys;
import io.openmessaging.extension.QueueMetaData;
import io.openmessaging.journalq.consumer.ExtensionConsumer;
import io.openmessaging.message.Message;

/**
 * PartitionReceiveConsumer
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/2/20
 */
public class PartitionReceiveConsumer {

    public static void main(String[] args) throws Exception {
        KeyValue keyValue = OMS.newKeyValue();
        keyValue.put(OMSBuiltinKeys.ACCOUNT_KEY, "test_token");

        MessagingAccessPoint messagingAccessPoint = OMS.getMessagingAccessPoint(String.format("oms:journalq://test_app@%s:50088/UNKNOWN", IpUtil.getLocalIp()), keyValue);

        // 首先需要对consumer强制转型
        ExtensionConsumer extensionConsumer = (ExtensionConsumer) messagingAccessPoint.createConsumer();
        extensionConsumer.start();

        // 绑定主题，将要消费的主题
        extensionConsumer.bindQueue("test_topic_0");

        // 获取元数据
        QueueMetaData queueMetaData = extensionConsumer.getQueueMetaData("test_topic_0");

        // 这里只是简单例子，根据具体情况进行调度处理
        while (true) {
            // 循环所有partition，并拉取相应消息
            for (QueueMetaData.Partition partition : queueMetaData.partitions()) {
                System.out.println("doReceive");

                // 拉取分区单条消息
                // 参数是超时时间，只是网络请求的超时
                Message message = extensionConsumer.receive((short) partition.partitionId(), 1000 * 10);

                // 批量拉取的方式相同，不单独列出
//                List<Message> messages = extensionConsumer.batchReceive((short) partition.partitionId(), 1000 * 10);

                // 没有拉取到，继续循环
                if (message == null) {
                    continue;
                }

                // 拉取到消息，打印日志并ack
                System.out.println(String.format("receive, message: %s", message));
                extensionConsumer.ack(message.getMessageReceipt());
            }

            Thread.currentThread().sleep(1000 * 1);
        }
    }
}
