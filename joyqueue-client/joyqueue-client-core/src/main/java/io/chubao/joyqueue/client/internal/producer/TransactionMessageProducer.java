package io.chubao.joyqueue.client.internal.producer;

import io.chubao.joyqueue.client.internal.producer.domain.ProduceMessage;
import io.chubao.joyqueue.client.internal.producer.domain.SendResult;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * TransactionMessageProducer
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/10
 */
public interface TransactionMessageProducer {

    void commit();

    void rollback();

    SendResult send(ProduceMessage message);

    SendResult send(ProduceMessage message, long timeout, TimeUnit timeoutUnit);

    List<SendResult> batchSend(List<ProduceMessage> messages);

    List<SendResult> batchSend(List<ProduceMessage> messages, long timeout, TimeUnit timeoutUnit);
}