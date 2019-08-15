package io.chubao.joyqueue.client.internal.producer.callback;

import io.chubao.joyqueue.client.internal.producer.domain.ProduceMessage;
import io.chubao.joyqueue.client.internal.producer.domain.SendResult;

/**
 * AsyncProduceCallback
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/10
 */
public interface AsyncProduceCallback {

    void onSuccess(ProduceMessage message, SendResult result);

    void onException(ProduceMessage message, Throwable cause);
}