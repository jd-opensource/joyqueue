package io.openmessaging.joyqueue.consumer.support;

import io.openmessaging.consumer.MessageListener;

/**
 * MessageListenerContextAdapter
 *
 * author: gaohaoxiang
 * date: 2019/2/20
 */
public class MessageListenerContextAdapter implements MessageListener.Context {

    private boolean ack = false;

    @Override
    public void ack() {
        ack = true;
    }

    public boolean isAck() {
        return ack;
    }
}