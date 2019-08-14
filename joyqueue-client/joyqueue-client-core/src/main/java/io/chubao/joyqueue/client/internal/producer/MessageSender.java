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
package io.chubao.joyqueue.client.internal.producer;

import io.chubao.joyqueue.client.internal.producer.callback.AsyncBatchSendCallback;
import io.chubao.joyqueue.client.internal.producer.callback.AsyncMultiBatchSendCallback;
import io.chubao.joyqueue.client.internal.producer.callback.AsyncSendCallback;
import io.chubao.joyqueue.client.internal.producer.domain.FetchFeedbackData;
import io.chubao.joyqueue.client.internal.producer.domain.ProduceMessage;
import io.chubao.joyqueue.client.internal.producer.domain.SendBatchResultData;
import io.chubao.joyqueue.client.internal.producer.domain.SendPrepareResult;
import io.chubao.joyqueue.client.internal.producer.domain.SendResultData;
import io.chubao.joyqueue.domain.QosLevel;
import io.chubao.joyqueue.exception.JoyQueueCode;
import io.chubao.joyqueue.network.command.TxStatus;
import io.chubao.joyqueue.network.domain.BrokerNode;
import io.chubao.joyqueue.toolkit.lang.LifeCycle;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * MessageSender
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/10
 */
public interface MessageSender extends LifeCycle {

    SendResultData send(BrokerNode brokerNode, String topic, String app, String txId, ProduceMessage message, QosLevel qosLevel,
                        long produceTimeout, long timeout);

    SendBatchResultData batchSend(BrokerNode brokerNode, String topic, String app, String txId, List<ProduceMessage> messages,
                                  QosLevel qosLevel, long produceTimeout, long timeout);

    void sendAsync(BrokerNode brokerNode, String topic, String app, String txId, ProduceMessage message, QosLevel qosLevel,
                   long produceTimeout, long timeout, AsyncSendCallback callback);

    void batchSendAsync(BrokerNode brokerNode, String topic, String app, String txId, List<ProduceMessage> messages,
                        QosLevel qosLevel, long produceTimeout, long timeout, AsyncBatchSendCallback callback);

    CompletableFuture<SendBatchResultData> batchSendAsync(BrokerNode brokerNode, String topic, String app, String txId,
                                                          List<ProduceMessage> messages, QosLevel qosLevel, long produceTimeout, long timeout);

    // oneway
    void sendOneway(BrokerNode brokerNode, String topic, String app, String txId, ProduceMessage message, QosLevel qosLevel,
                    long produceTimeout, long timeout);

    void batchSendOneway(BrokerNode brokerNode, String topic, String app, String txId, List<ProduceMessage> messages,
                         QosLevel qosLevel, long produceTimeout, long timeout);

    void batchSendOneway(BrokerNode brokerNode, String app, String txId, Map<String, List<ProduceMessage>> messages,
                         QosLevel qosLevel, long produceTimeout, long timeout);

    // batch
    Map<String, SendBatchResultData> batchSend(BrokerNode brokerNode, String app, String txId, Map<String, List<ProduceMessage>> messages,
                                               QosLevel qosLevel, long produceTimeout, long timeout);

    void batchSendAsync(BrokerNode brokerNode, String app, String txId, Map<String, List<ProduceMessage>> messages,
                        QosLevel qosLevel, long produceTimeout, long timeout, AsyncMultiBatchSendCallback callback);

    CompletableFuture<Map<String, SendBatchResultData>> batchSendAsync(BrokerNode brokerNode, String app, String txId,
                                                                       Map<String, List<ProduceMessage>> messages, QosLevel qosLevel,
                                                                       long produceTimeout, long timeout);

    // transaction
    SendPrepareResult prepare(BrokerNode brokerNode, String topic, String app, String transactionId, long sequence, long transactionTimeout, long timeout);

    JoyQueueCode commit(BrokerNode brokerNode, String topic, String app, String txId, long timeout);

    JoyQueueCode rollback(BrokerNode brokerNode, String topic, String app, String txId, long timeout);

    FetchFeedbackData fetchFeedback(BrokerNode brokerNode, String topic, String app, TxStatus txStatus, int count, long longPollTimeout, long timeout);
}