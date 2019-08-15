package io.chubao.joyqueue.client.internal.consumer.support;

import io.chubao.joyqueue.client.internal.consumer.callback.PollerListener;
import io.chubao.joyqueue.client.internal.consumer.domain.ConsumeMessage;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * CompletableFuturePollerListener
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/7/25
 */
public class CompletableFuturePollerListener implements PollerListener {

    private CompletableFuture<List<ConsumeMessage>> future;

    public CompletableFuturePollerListener(CompletableFuture<List<ConsumeMessage>> future) {
        this.future = future;
    }

    @Override
    public void onMessage(List<ConsumeMessage> messages) {
        future.complete(messages);
    }

    @Override
    public void onException(Throwable cause) {
        future.completeExceptionally(cause);
    }
}