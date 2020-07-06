/**
 * Copyright 2019 The JoyQueue Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.joyqueue.client.internal.producer.callback;

import org.joyqueue.client.internal.producer.domain.ProduceMessage;
import org.joyqueue.client.internal.producer.domain.SendResult;

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