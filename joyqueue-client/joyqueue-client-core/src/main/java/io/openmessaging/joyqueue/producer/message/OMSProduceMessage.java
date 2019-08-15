package io.openmessaging.joyqueue.producer.message;

import io.chubao.joyqueue.client.internal.producer.domain.ProduceMessage;
import io.openmessaging.message.Message;

/**
 * OMSProduceMessage
 *
 * author: gaohaoxiang
 * date: 2019/3/1
 */
public class OMSProduceMessage extends ProduceMessage {

    private Message omsMessage;

    public void setOmsMessage(Message omsMessage) {
        this.omsMessage = omsMessage;
    }

    public Message getOmsMessage() {
        return omsMessage;
    }
}