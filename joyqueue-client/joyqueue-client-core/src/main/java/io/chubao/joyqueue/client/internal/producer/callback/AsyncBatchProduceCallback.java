package io.chubao.joyqueue.client.internal.producer.callback;

import io.chubao.joyqueue.client.internal.producer.domain.ProduceMessage;
import io.chubao.joyqueue.client.internal.producer.domain.SendResult;

import java.util.List;

/**
 * AsyncBatchProduceCallback
 *
 * author: gaohaoxiang
 * date: 2018/12/20
 */
public interface AsyncBatchProduceCallback {

    void onSuccess(List<ProduceMessage> messages, List<SendResult> result);

    void onException(List<ProduceMessage> messages, Throwable cause);
}