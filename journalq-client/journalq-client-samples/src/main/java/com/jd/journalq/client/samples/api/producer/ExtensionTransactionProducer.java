package com.jd.journalq.client.samples.api.producer;

import com.jd.journalq.toolkit.network.IpUtil;
import io.openmessaging.KeyValue;
import io.openmessaging.MessagingAccessPoint;
import io.openmessaging.OMS;
import io.openmessaging.journalq.JMQBuiltinKeys;
import io.openmessaging.journalq.producer.ExtensionProducer;
import io.openmessaging.journalq.producer.ExtensionTransactionalResult;
import io.openmessaging.message.Message;
import io.openmessaging.producer.SendResult;

/**
 * FutureProducer
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/2/19
 */
public class ExtensionTransactionProducer {

    public static void main(String[] args) throws Exception {
        KeyValue keyValue = OMS.newKeyValue();
        keyValue.put(JMQBuiltinKeys.ACCOUNT_KEY, "test_token");

        MessagingAccessPoint messagingAccessPoint = OMS.getMessagingAccessPoint(String.format("oms:jmq://test_app@%s:50088/UNKNOWN", IpUtil.getLocalIp()), keyValue);

        ExtensionProducer producer = (ExtensionProducer) messagingAccessPoint.createProducer();
        producer.start();

        ExtensionTransactionalResult transactionalResult = producer.prepare();

        // 可以发多条事务消息
        Message message = producer.createMessage("test_topic_0", "body".getBytes());

        SendResult sendResult = transactionalResult.send(message);
        System.out.println(sendResult.messageId());

        sendResult = transactionalResult.send(message);
        System.out.println(sendResult.messageId());

        transactionalResult.commit();

        System.in.read();
    }
}