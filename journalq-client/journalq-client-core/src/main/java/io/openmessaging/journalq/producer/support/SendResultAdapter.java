package io.openmessaging.journalq.producer.support;

import io.openmessaging.producer.SendResult;

/**
 * SendResultAdapter
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/2/19
 */
public class SendResultAdapter implements SendResult {

    private com.jd.journalq.client.internal.producer.domain.SendResult sendResult;

    public SendResultAdapter(com.jd.journalq.client.internal.producer.domain.SendResult sendResult) {
        this.sendResult = sendResult;
    }

    @Override
    public String messageId() {
        return null;
    }

    @Override
    public String toString() {
        return sendResult.toString();
    }
}