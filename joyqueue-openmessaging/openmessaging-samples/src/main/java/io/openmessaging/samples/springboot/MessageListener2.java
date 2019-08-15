package io.openmessaging.samples.springboot;

import io.openmessaging.consumer.MessageListener;
import io.openmessaging.message.Message;
import io.openmessaging.spring.boot.annotation.OMSMessageListener;
import org.springframework.stereotype.Component;

@Component
@OMSMessageListener(queueName = "test_topic_2")
public class MessageListener2 implements MessageListener {

    @Override
    public void onReceived(Message message, MessageListener.Context context) {
        System.out.println(String.format("receive, message: %s", message));
        context.ack();
    }
}