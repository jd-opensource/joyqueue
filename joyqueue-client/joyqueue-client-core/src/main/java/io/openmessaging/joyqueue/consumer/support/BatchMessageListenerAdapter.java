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
package io.openmessaging.joyqueue.consumer.support;

import org.joyqueue.client.internal.consumer.BatchMessageListener;
import org.joyqueue.client.internal.consumer.domain.ConsumeMessage;
import org.joyqueue.client.internal.consumer.exception.IgnoreAckException;
import io.openmessaging.joyqueue.consumer.message.MessageConverter;
import io.openmessaging.message.Message;

import java.util.List;

/**
 * BatchMessageListenerAdapter
 *
 * author: gaohaoxiang
 * date: 2019/2/20
 */
public class BatchMessageListenerAdapter implements BatchMessageListener {

    private io.openmessaging.consumer.BatchMessageListener omsBatchMessageListener;

    public BatchMessageListenerAdapter(io.openmessaging.consumer.BatchMessageListener omsBatchMessageListener) {
        this.omsBatchMessageListener = omsBatchMessageListener;
    }

    @Override
    public void onMessage(List<ConsumeMessage> messages) {
        BatchMessageListenerContextAdapter context = new BatchMessageListenerContextAdapter();
        List<Message> omsMessages = MessageConverter.convertMessages(messages);
        omsBatchMessageListener.onReceived(omsMessages, context);

        if (!context.isAck()) {
            throw new IgnoreAckException();
        }
    }

    @Override
    public int hashCode() {
        return omsBatchMessageListener.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return omsBatchMessageListener.equals(obj);
    }

    @Override
    public String toString() {
        return omsBatchMessageListener.toString();
    }
}