/**
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
package com.jd.journalq.client.internal.consumer;

import com.jd.journalq.client.internal.consumer.callback.ConsumerListener;
import com.jd.journalq.client.internal.consumer.domain.ConsumeMessage;
import com.jd.journalq.client.internal.consumer.domain.ConsumeReply;
import com.jd.journalq.client.internal.metadata.domain.TopicMetadata;
import com.jd.journalq.exception.JournalqCode;
import com.jd.journalq.toolkit.lang.LifeCycle;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * MessagePoller
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/11
 */
public interface MessagePoller extends LifeCycle {

    // poll
    ConsumeMessage pollOnce(String topic);

    ConsumeMessage pollOnce(String topic, long timeout, TimeUnit timeoutUnit);

    List<ConsumeMessage> poll(String topic);

    List<ConsumeMessage> poll(String topic, long timeout, TimeUnit timeoutUnit);

    void pollAsync(String topic, ConsumerListener listener);

    void pollAsync(String topic, long timeout, TimeUnit timeoutUnit, ConsumerListener listener);

    // poll partition
    ConsumeMessage pollPartitionOnce(String topic, short partition);

    ConsumeMessage pollPartitionOnce(String topic, short partition, long timeout, TimeUnit timeoutUnit);

    ConsumeMessage pollPartitionOnce(String topic, short partition, long index);

    ConsumeMessage pollPartitionOnce(String topic, short partition, long index, long timeout, TimeUnit timeoutUnit);

    List<ConsumeMessage> pollPartition(String topic, short partition);

    List<ConsumeMessage> pollPartition(String topic, short partition, long timeout, TimeUnit timeoutUnit);

    List<ConsumeMessage> pollPartition(String topic, short partition, long index);

    List<ConsumeMessage> pollPartition(String topic, short partition, long index, long timeout, TimeUnit timeoutUnit);

    void pollPartitionAsync(String topic, short partition, ConsumerListener listener);

    void pollPartitionAsync(String topic, short partition, long timeout, TimeUnit timeoutUnit, ConsumerListener listener);

    void pollPartitionAsync(String topic, short partition, long index, ConsumerListener listener);

    void pollPartitionAsync(String topic, short partition, long index, long timeout, TimeUnit timeoutUnit, ConsumerListener listener);

    // reply
    JournalqCode reply(String topic, List<ConsumeReply> replyList);

    JournalqCode replyOnce(String topic, ConsumeReply reply);

    // metadata
    TopicMetadata getTopicMetadata(String topic);
}