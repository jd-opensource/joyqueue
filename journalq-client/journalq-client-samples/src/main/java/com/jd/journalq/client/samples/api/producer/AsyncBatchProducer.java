package com.jd.journalq.client.samples.api.producer;

import com.jd.journalq.toolkit.network.IpUtil;
import io.openmessaging.Future;
import io.openmessaging.KeyValue;
import io.openmessaging.MessagingAccessPoint;
import io.openmessaging.OMS;
import io.openmessaging.OMSBuiltinKeys;
import io.openmessaging.message.Message;
import io.openmessaging.producer.Producer;
import io.openmessaging.producer.SendResult;

import java.util.ArrayList;
import java.util.List;

/**
 * AsyncBatchProducer
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/4/8
 */
public class AsyncBatchProducer {

    public static void main(String[] args) {
        KeyValue keyValue = OMS.newKeyValue();
        keyValue.put(OMSBuiltinKeys.ACCOUNT_KEY, "test_token");

        MessagingAccessPoint messagingAccessPoint = OMS.getMessagingAccessPoint(String.format("oms:journalq://test_app@%s:50088/UNKNOWN", IpUtil.getLocalIp()), keyValue);

        Producer producer = messagingAccessPoint.createProducer();
        producer.start();

        List<Message> messages = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            Message message = producer.createMessage("test_topic_0", "body".getBytes());
            messages.add(message);
        }

        Future<SendResult> future = producer.sendAsync(messages);

//        future.addListener(new FutureListener<SendResult>() {
//            @Override
//            public void operationComplete(Future<SendResult> future) {
//                System.out.println(future.get().messageId());
//            }
//        });
        System.out.println(future.get().messageId());
    }
}