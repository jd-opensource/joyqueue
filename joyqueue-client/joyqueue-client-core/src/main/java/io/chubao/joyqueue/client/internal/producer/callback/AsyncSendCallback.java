package io.chubao.joyqueue.client.internal.producer.callback;

import io.chubao.joyqueue.client.internal.producer.domain.ProduceMessage;
import io.chubao.joyqueue.client.internal.producer.domain.SendResultData;

/**
 * AsyncSendCallback
 *
 * author: gaohaoxiang
 * date: 2018/12/10
 */
public interface AsyncSendCallback {

    void onSuccess(ProduceMessage message, SendResultData result);

    void onException(ProduceMessage message, Throwable cause);
}