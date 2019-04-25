package com.jd.journalq.client.samples.api.producer;

import com.jd.journalq.toolkit.network.IpUtil;
import io.openmessaging.KeyValue;
import io.openmessaging.MessagingAccessPoint;
import io.openmessaging.OMS;
import io.openmessaging.OMSBuiltinKeys;
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
        keyValue.put(OMSBuiltinKeys.ACCOUNT_KEY, "test_token");

        MessagingAccessPoint messagingAccessPoint = OMS.getMessagingAccessPoint(String.format("oms:journalq://test_app@%s:50088/UNKNOWN", IpUtil.getLocalIp()), keyValue);

        ExtensionProducer extensionProducer = (ExtensionProducer) messagingAccessPoint.createProducer();
        extensionProducer.start();

        ExtensionTransactionalResult transactionalResult = extensionProducer.prepare();

        for (int i = 0; i < 10; i++) {
            // 可以发多条事务消息
            Message message = extensionProducer.createMessage("test_topic_0", "body".getBytes());

            // 添加事务id，设置过事务id的才会被补偿，补偿时会带上这个事务id，非必填
            // 建议根据业务使用有意义的事务id
            message.extensionHeader().get().setTransactionId("test_transactionId");

            SendResult sendResult = transactionalResult.send(message);
            System.out.println(sendResult.messageId());
        }

        // 提交事务
        transactionalResult.commit();

        // 回滚事务
//        transactionalResult.rollback();
    }
}