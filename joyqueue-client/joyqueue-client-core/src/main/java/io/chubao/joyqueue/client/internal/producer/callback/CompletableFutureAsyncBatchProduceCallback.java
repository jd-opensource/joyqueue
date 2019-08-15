package io.chubao.joyqueue.client.internal.producer.callback;

import io.chubao.joyqueue.client.internal.producer.domain.ProduceMessage;
import io.chubao.joyqueue.client.internal.producer.domain.SendResult;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * CompletableFutureAsyncBatchProduceCallback
 *
 * author: gaohaoxiang
 * date: 2019/7/25
 */
public class CompletableFutureAsyncBatchProduceCallback implements AsyncBatchProduceCallback {

    private CompletableFuture<List<SendResult>> future;

    public CompletableFutureAsyncBatchProduceCallback(CompletableFuture<List<SendResult>> future) {
        this.future = future;
    }

    @Override
    public void onSuccess(List<ProduceMessage> messages, List<SendResult> result) {
        future.complete(result);
    }

    @Override
    public void onException(List<ProduceMessage> messages, Throwable cause) {
        future.completeExceptionally(cause);
    }
}