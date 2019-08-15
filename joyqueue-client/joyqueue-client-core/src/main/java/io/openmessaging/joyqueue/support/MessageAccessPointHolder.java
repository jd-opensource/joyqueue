package io.openmessaging.joyqueue.support;

import io.chubao.joyqueue.client.internal.MessageAccessPoint;

/**
 * MessageAccessPointHolder
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/5/14
 */
public class MessageAccessPointHolder {

    private MessageAccessPoint messageAccessPoint;
    private int producers = 0;
    private int consumers = 0;

    public MessageAccessPointHolder(MessageAccessPoint messageAccessPoint) {
        this.messageAccessPoint = messageAccessPoint;
    }

    public MessageAccessPoint getMessageAccessPoint() {
        return messageAccessPoint;
    }

    public void stopProducer() {
        producers--;
        maybeStop();
    }

    public void startProducer() {
        producers++;
    }

    public void stopConsumer() {
        consumers--;
        maybeStop();
    }

    public void startConsumer() {
        consumers++;
    }

    protected void maybeStop() {
        if (producers != 0 || consumers != 0) {
            return;
        }
        messageAccessPoint.stop();
    }
}