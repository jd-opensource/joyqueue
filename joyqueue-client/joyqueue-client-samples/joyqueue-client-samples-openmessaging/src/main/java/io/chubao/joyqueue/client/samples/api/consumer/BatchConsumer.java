package io.chubao.joyqueue.client.samples.api.consumer;

import io.openmessaging.KeyValue;
import io.openmessaging.MessagingAccessPoint;
import io.openmessaging.OMS;
import io.openmessaging.OMSBuiltinKeys;
import io.openmessaging.consumer.BatchMessageListener;
import io.openmessaging.consumer.Consumer;
import io.openmessaging.message.Message;

import java.util.List;

/**
 * BatchConsumer
 *
 * author: gaohaoxiang
 * date: 2019/4/8
 */
public class BatchConsumer {

    public static void main(String[] args) throws Exception {
        KeyValue keyValue = OMS.newKeyValue();
        keyValue.put(OMSBuiltinKeys.ACCOUNT_KEY, "test_token");

        MessagingAccessPoint messagingAccessPoint = OMS.getMessagingAccessPoint("oms:joyqueue://test_app@127.0.0.1:50088/UNKNOWN", keyValue);

        Consumer consumer = messagingAccessPoint.createConsumer();

        consumer.bindQueue("test_topic_0", new BatchMessageListener() {
            @Override
            public void onReceived(List<Message> messages, Context context) {
                for (Message message : messages) {
                    System.out.println(String.format("onReceived, message: %s", message));
                }

                // 代表这一批消息的ack
                context.ack();
            }
        });

        consumer.start();
        System.in.read();
    }
}
