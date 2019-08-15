package io.openmessaging.joyqueue.producer.support;

import io.openmessaging.producer.SendResult;

/**
 * SendResultAdapter
 *
 * author: gaohaoxiang
 * date: 2019/2/19
 */
public class SendResultAdapter implements SendResult {

    private io.chubao.joyqueue.client.internal.producer.domain.SendResult sendResult;

    public SendResultAdapter(io.chubao.joyqueue.client.internal.producer.domain.SendResult sendResult) {
        this.sendResult = sendResult;
    }

    @Override
    public String messageId() {
        return String.valueOf(sendResult.getIndex());
    }

    @Override
    public String toString() {
        return sendResult.toString();
    }
}