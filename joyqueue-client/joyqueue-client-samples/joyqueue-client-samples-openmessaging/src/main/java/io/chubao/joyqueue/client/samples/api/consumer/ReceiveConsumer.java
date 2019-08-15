package io.chubao.joyqueue.client.samples.api.consumer;

import io.openmessaging.KeyValue;
import io.openmessaging.MessagingAccessPoint;
import io.openmessaging.OMS;
import io.openmessaging.OMSBuiltinKeys;
import io.openmessaging.consumer.Consumer;
import io.openmessaging.message.Message;

/**
 * ReceiveConsumer
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/2/20
 */
public class ReceiveConsumer {

    public static void main(String[] args) throws Exception {
        KeyValue keyValue = OMS.newKeyValue();
        keyValue.put(OMSBuiltinKeys.ACCOUNT_KEY, "test_token");

        MessagingAccessPoint messagingAccessPoint = OMS.getMessagingAccessPoint("oms:joyqueue://test_app@127.0.0.1:50088/UNKNOWN", keyValue);

        Consumer consumer = messagingAccessPoint.createConsumer();
        consumer.start();

        // 绑定主题，将要消费的主题
        consumer.bindQueue("test_topic_0");

        while (true) {
            System.out.println("doReceive");
            // 拉取单条消息
            // 参数是超时时间，只是网络请求的超时
            Message message = consumer.receive(1000 * 10);

            // 没有拉取到，继续循环
            if (message == null) {
                continue;
            }

            // 拉取到消息，打印日志并ack
            System.out.println(String.format("receive, message: %s", message));
            consumer.ack(message.getMessageReceipt());

            Thread.currentThread().sleep(1000 * 1);
        }
    }
}
