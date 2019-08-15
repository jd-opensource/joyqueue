package io.openmessaging.samples.spring;

import io.openmessaging.consumer.MessageListener;
import io.openmessaging.message.Message;

public class MessageListener1 implements MessageListener {

    @Override
    public void onReceived(Message message, Context context) {
        System.out.println(String.format("receive, message: %s", message));
    }
}