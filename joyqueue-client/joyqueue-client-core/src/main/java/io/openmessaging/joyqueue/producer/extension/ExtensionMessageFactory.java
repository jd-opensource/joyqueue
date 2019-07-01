package io.openmessaging.joyqueue.producer.extension;

import io.openmessaging.message.Message;
import io.openmessaging.message.MessageFactory;

/**
 * ExtensionMessageFactory
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/7/1
 */
public interface ExtensionMessageFactory extends MessageFactory {

    Message createMessage(String queueName, String body);
}