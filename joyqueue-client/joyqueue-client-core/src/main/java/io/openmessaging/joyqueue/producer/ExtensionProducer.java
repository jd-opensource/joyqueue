package io.openmessaging.joyqueue.producer;

import io.openmessaging.joyqueue.producer.extension.ExtensionMessageFactory;
import io.openmessaging.producer.Producer;

/**
 * ExtensionProducer
 *
 * author: gaohaoxiang
 * date: 2019/3/4
 */
public interface ExtensionProducer extends Producer, ExtensionMessageFactory {

    ExtensionTransactionalResult prepare();

    ExtensionTransactionalResult prepare(String transactionId);
}