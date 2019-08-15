package io.openmessaging.joyqueue.consumer.message;

import io.chubao.joyqueue.client.internal.consumer.domain.ConsumeMessage;
import io.openmessaging.consumer.MessageReceipt;

/**
 * MessageReceiptAdapter
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/3/1
 */
public class MessageReceiptAdapter implements MessageReceipt {

    private ConsumeMessage message;

    public MessageReceiptAdapter(ConsumeMessage message) {
        this.message = message;
    }

    public ConsumeMessage getMessage() {
        return message;
    }
}