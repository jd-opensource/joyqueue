package io.chubao.joyqueue.client.internal.producer.callback;

import io.chubao.joyqueue.client.internal.producer.domain.ProduceMessage;
import io.chubao.joyqueue.client.internal.producer.domain.SendResult;

import java.util.concurrent.CompletableFuture;

/**
 * CompletableFutureAsyncProduceCallback
 *
 * author: gaohaoxiang
 * date: 2019/7/25
 */
public class CompletableFutureAsyncProduceCallback implements AsyncProduceCallback {

    private CompletableFuture<SendResult> future;

    public CompletableFutureAsyncProduceCallback(CompletableFuture<SendResult> future) {
        this.future = future;
    }

    @Override
    public void onSuccess(ProduceMessage message, SendResult result) {
        future.complete(result);
    }

    @Override
    public void onException(ProduceMessage message, Throwable cause) {
        future.completeExceptionally(cause);
    }
}