package io.chubao.joyqueue.client.samples.springboot;

import io.openmessaging.consumer.BatchMessageListener;
import io.openmessaging.consumer.MessageListener;
import io.openmessaging.message.Message;
import io.openmessaging.spring.boot.annotation.OMSMessageListener;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * SimpleMessageListener
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/3/6
 */
@Component
public class SimpleMessageListener1 {

    @OMSMessageListener(queueName = "test_topic_0")
    public void onReceived(Message message, MessageListener.Context context) {
        System.out.println(String.format("receive, message: %s", message));
        context.ack();
    }

    @OMSMessageListener(queueName = "test_topic_1")
    public void onReceived(List<Message> messages, BatchMessageListener.Context context) {
        for (Message message : messages) {
            System.out.println(String.format("receive, message: %s", message));
        }
        context.ack();
    }
}