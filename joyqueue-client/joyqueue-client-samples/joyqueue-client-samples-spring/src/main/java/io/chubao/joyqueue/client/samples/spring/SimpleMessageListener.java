package io.chubao.joyqueue.client.samples.spring;

import io.openmessaging.consumer.MessageListener;
import io.openmessaging.message.Message;

/**
 * SimpleMessageListener
 *
 * author: gaohaoxiang
 * date: 2019/3/6
 */
public class SimpleMessageListener implements MessageListener {

    @Override
    public void onReceived(Message message, Context context) {
        System.out.println(String.format("receive, message: %s", message));
    }
}