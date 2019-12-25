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