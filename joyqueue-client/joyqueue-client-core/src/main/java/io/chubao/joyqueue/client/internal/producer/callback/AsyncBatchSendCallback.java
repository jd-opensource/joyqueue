package io.chubao.joyqueue.client.internal.producer.callback;

import io.chubao.joyqueue.client.internal.producer.domain.ProduceMessage;
import io.chubao.joyqueue.client.internal.producer.domain.SendBatchResultData;

import java.util.List;

/**
 * AsyncBatchSendCallback
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/20
 */
public interface AsyncBatchSendCallback {

    void onSuccess(List<ProduceMessage> messages, SendBatchResultData result);

    void onException(List<ProduceMessage> messages, Throwable cause);
}