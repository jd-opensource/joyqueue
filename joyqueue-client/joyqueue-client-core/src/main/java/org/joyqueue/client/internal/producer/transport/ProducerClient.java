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
package org.joyqueue.client.internal.producer.transport;

import org.joyqueue.client.internal.transport.Client;
import org.joyqueue.client.internal.transport.ClientState;
import org.joyqueue.network.command.FetchProduceFeedbackRequest;
import org.joyqueue.network.command.FetchProduceFeedbackResponse;
import org.joyqueue.network.command.ProduceMessageCommitRequest;
import org.joyqueue.network.command.ProduceMessageCommitResponse;
import org.joyqueue.network.command.ProduceMessageData;
import org.joyqueue.network.command.ProduceMessagePrepareRequest;
import org.joyqueue.network.command.ProduceMessagePrepareResponse;
import org.joyqueue.network.command.ProduceMessageRequest;
import org.joyqueue.network.command.ProduceMessageResponse;
import org.joyqueue.network.command.ProduceMessageRollbackRequest;
import org.joyqueue.network.command.ProduceMessageRollbackResponse;
import org.joyqueue.network.command.TxStatus;
import org.joyqueue.network.transport.TransportAttribute;
import org.joyqueue.network.transport.command.CommandCallback;
import org.joyqueue.network.transport.command.JoyQueueCommand;

import java.util.Collection;
import java.util.Map;

/**
 * ProducerClient
 *
 * author: gaohaoxiang
 * date: 2018/11/29
 */
public class ProducerClient {

    private static final String CLIENT_PRODUCER_CACHE_KEY = "_CLIENT_PRODUCER_CACHE_";

    private Client client;

    private ProducerConnectionState connectionState;

    public static ProducerClient build(Client client) {
        ProducerClient producerClient = client.getAttribute().get(CLIENT_PRODUCER_CACHE_KEY);
        if (producerClient == null) {
            producerClient = new ProducerClient(client);
            ProducerClient oldProducerClient = client.getAttribute().putIfAbsent(CLIENT_PRODUCER_CACHE_KEY, producerClient);
            if (oldProducerClient != null) {
                producerClient = oldProducerClient;
            } else {
                producerClient.getClient().addListener(new ProducerClientConnectionListener(producerClient.getClient().getTransport(), producerClient));
            }
        }
        return producerClient;
    }

    public ProducerClient(Client client) {
        this.client = client;
        this.connectionState = new ProducerConnectionState(this);
    }

    public ProduceMessageResponse produceMessage(String app, Map<String, ProduceMessageData> messages, long timeout) {
        ProduceMessageRequest produceMessageRequest = new ProduceMessageRequest();
        produceMessageRequest.setApp(app);
        produceMessageRequest.setData(messages);
        return (ProduceMessageResponse) client.sync(new JoyQueueCommand(produceMessageRequest), timeout).getPayload();
    }

    public void produceMessageOneway(String app, Map<String, ProduceMessageData> messages, long timeout) {
        ProduceMessageRequest produceMessageRequest = new ProduceMessageRequest();
        produceMessageRequest.setApp(app);
        produceMessageRequest.setData(messages);
        client.oneway(new JoyQueueCommand(produceMessageRequest), timeout);
    }

    public void asyncProduceMessage(String app, Map<String, ProduceMessageData> messages, long timeout, CommandCallback callback) {
        ProduceMessageRequest produceMessageRequest = new ProduceMessageRequest();
        produceMessageRequest.setApp(app);
        produceMessageRequest.setData(messages);
        client.async(new JoyQueueCommand(produceMessageRequest), timeout, callback);
    }

    public ProduceMessagePrepareResponse produceMessagePrepare(String topic, String app, long sequence, String transactionId, long transactionTimeout, long timeout) {
        ProduceMessagePrepareRequest produceMessagePrepareRequest = new ProduceMessagePrepareRequest();
        produceMessagePrepareRequest.setTopic(topic);
        produceMessagePrepareRequest.setApp(app);
        produceMessagePrepareRequest.setSequence(sequence);
        produceMessagePrepareRequest.setTransactionId(transactionId);
        produceMessagePrepareRequest.setTimeout((int) transactionTimeout);
        return (ProduceMessagePrepareResponse) client.sync(new JoyQueueCommand(produceMessagePrepareRequest), timeout).getPayload();
    }

    public ProduceMessageCommitResponse produceMessageCommit(String topic, String app, String txId, long timeout) {
        ProduceMessageCommitRequest produceMessageCommitRequest = new ProduceMessageCommitRequest();
        produceMessageCommitRequest.setTopic(topic);
        produceMessageCommitRequest.setApp(app);
        produceMessageCommitRequest.setTxId(txId);
        return (ProduceMessageCommitResponse) client.sync(new JoyQueueCommand(produceMessageCommitRequest), timeout).getPayload();
    }

    public ProduceMessageRollbackResponse produceMessageRollback(String topic, String app, String txId, long timeout) {
        ProduceMessageRollbackRequest produceMessageRollbackRequest = new ProduceMessageRollbackRequest();
        produceMessageRollbackRequest.setTopic(topic);
        produceMessageRollbackRequest.setApp(app);
        produceMessageRollbackRequest.setTxId(txId);
        return (ProduceMessageRollbackResponse) client.sync(new JoyQueueCommand(produceMessageRollbackRequest), timeout).getPayload();
    }

    public FetchProduceFeedbackResponse fetchFeedback(String topic, String app, TxStatus txStatus, int count, long longPollTimeout, long timeout) {
        FetchProduceFeedbackRequest fetchProduceFeedbackRequest = new FetchProduceFeedbackRequest();
        fetchProduceFeedbackRequest.setTopic(topic);
        fetchProduceFeedbackRequest.setApp(app);
        fetchProduceFeedbackRequest.setStatus(txStatus);
        fetchProduceFeedbackRequest.setCount(count);
        fetchProduceFeedbackRequest.setLongPollTimeout((int) longPollTimeout);
        return (FetchProduceFeedbackResponse) client.sync(new JoyQueueCommand(fetchProduceFeedbackRequest), timeout).getPayload();
    }

    public void addProducers() {
        connectionState.handleAddProducers();
    }

    public void addProducers(Collection<String> topics, String app) {
        connectionState.handleAddProducers(topics, app);
    }

    public void removeProducers(Collection<String> topics, String app) {
        connectionState.handleRemoveProducers(topics, app);
    }

    public void close() {
        connectionState.handleRemoveProducers();
        client.stop();
    }

    public TransportAttribute getAttribute() {
        return client.getAttribute();
    }

    public Client getClient() {
        return client;
    }

    public ClientState getState() {
        return client.getState();
    }
}