package com.jd.journalq.client.samples.springboot;

import io.openmessaging.consumer.MessageListener;
import io.openmessaging.message.Message;
import io.openmessaging.spring.boot.annotation.OMSMessageListener;
import org.springframework.stereotype.Component;

/**
 * MessageListener2
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/3/6
 */
@Component
@OMSMessageListener(queueName = "test_topic_2")
public class SimpleMessageListener2 implements MessageListener {

    @Override
    public void onReceived(Message message, MessageListener.Context context) {
        System.out.println(String.format("receive, message: %s", message));
        context.ack();
    }
}