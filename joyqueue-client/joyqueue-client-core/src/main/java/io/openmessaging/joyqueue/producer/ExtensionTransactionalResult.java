package io.openmessaging.joyqueue.producer;

import io.openmessaging.message.Message;
import io.openmessaging.producer.SendResult;

import java.util.List;

/**
 * ExtensionTransactionalResult
 *
 * author: gaohaoxiang
 * date: 2019/3/4
 */
public interface ExtensionTransactionalResult {

    String transactionId();

    void commit();

    void rollback();

    SendResult send(Message message);

    List<SendResult> send(List<Message> messages);
}