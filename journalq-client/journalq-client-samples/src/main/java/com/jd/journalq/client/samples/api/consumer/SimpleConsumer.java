package com.jd.journalq.client.samples.api.consumer;

import com.jd.journalq.toolkit.network.IpUtil;
import io.openmessaging.KeyValue;
import io.openmessaging.MessagingAccessPoint;
import io.openmessaging.OMS;
import io.openmessaging.OMSBuiltinKeys;
import io.openmessaging.consumer.Consumer;
import io.openmessaging.consumer.MessageListener;
import io.openmessaging.message.Message;

/**
 * SimpleConsumer
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/2/20
 */
public class SimpleConsumer {

    public static void main(String[] args) throws Exception {
        KeyValue keyValue = OMS.newKeyValue();
        keyValue.put(OMSBuiltinKeys.ACCOUNT_KEY, "test_token");

        MessagingAccessPoint messagingAccessPoint = OMS.getMessagingAccessPoint(String.format("oms:journalq://test_app@%s:50088/UNKNOWN", IpUtil.getLocalIp()), keyValue);

        // 创建consumer实例
        Consumer consumer = messagingAccessPoint.createConsumer();

        // 绑定需要消费的topic和对应的listener
        consumer.bindQueue("test_topic_0", new MessageListener() {
            @Override
            public void onReceived(Message message, Context context) {
                System.out.println(String.format("onReceived, message: %s", message));

                // 确认消息消费成功，如果没有确认或抛出异常会进入重试队列
                context.ack();
            }
        });

        consumer.start();
        System.in.read();
    }
}
