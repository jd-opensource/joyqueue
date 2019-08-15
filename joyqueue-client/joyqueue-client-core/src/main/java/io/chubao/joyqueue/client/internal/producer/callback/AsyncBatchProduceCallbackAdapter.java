package io.chubao.joyqueue.client.internal.producer.callback;

import io.chubao.joyqueue.client.internal.producer.domain.ProduceMessage;
import io.chubao.joyqueue.client.internal.producer.domain.SendResult;

import java.util.List;

/**
 * AsyncBatchProduceCallbackAdapter
 *
 * author: gaohaoxiang
 * date: 2018/12/27
 */
public class AsyncBatchProduceCallbackAdapter implements AsyncBatchProduceCallback {

    private AsyncProduceCallback callback;

    public AsyncBatchProduceCallbackAdapter(AsyncProduceCallback callback) {
        this.callback = callback;
    }

    @Override
    public void onSuccess(List<ProduceMessage> messages, List<SendResult> result) {
        callback.onSuccess(messages.get(0), result.get(0));
    }

    @Override
    public void onException(List<ProduceMessage> messages, Throwable cause) {
        callback.onException(messages.get(0), cause);
    }
}