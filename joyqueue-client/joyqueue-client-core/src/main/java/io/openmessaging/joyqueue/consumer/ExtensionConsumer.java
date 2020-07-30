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
package io.openmessaging.joyqueue.consumer;

import io.openmessaging.consumer.Consumer;
import io.openmessaging.consumer.MessageReceipt;
import io.openmessaging.message.Message;

import java.util.List;

/**
 * ExtensionConsumer
 *
 * author: gaohaoxiang
 * date: 2019/3/4
 */
public interface ExtensionConsumer extends Consumer {

    // receive
    Message receive(short partition, long timeout);

    List<Message> batchReceive(short partition, long timeout);

    Message receive(short partition, long index, long timeout);

    List<Message> batchReceive(short partition, long index, long timeout);

    // ack
    void batchAck(List<MessageReceipt> receiptList);

    // commit
    void commitIndex(short partition, long index);

    void commitMaxIndex(short partition);

    void commitMaxIndex();

    void commitMinIndex(short partition);

    void commitMinIndex();

    // index
    ConsumerIndex getIndex(short partition);
}