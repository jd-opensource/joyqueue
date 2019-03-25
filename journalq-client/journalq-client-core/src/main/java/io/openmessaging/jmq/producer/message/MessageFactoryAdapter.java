package io.openmessaging.jmq.producer.message;

import io.openmessaging.message.Message;
import io.openmessaging.message.MessageFactory;

/**
 * MessageFactoryAdapter
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/3/1
 */
public class MessageFactoryAdapter implements MessageFactory {

    @Override
    public Message createMessage(String queueName, byte[] body) {
        OMSProduceMessage omsProduceMessage = new OMSProduceMessage();
        omsProduceMessage.setTopic(queueName);
        omsProduceMessage.setBodyBytes(body);

        MessageAdapter messageAdapter = new MessageAdapter(omsProduceMessage);
        omsProduceMessage.setOmsMessage(messageAdapter);
        return messageAdapter;
    }
}