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
package com.jd.journalq.client.internal.producer.transport;

import com.jd.journalq.client.internal.transport.Client;
import com.jd.journalq.client.internal.transport.ClientState;
import com.jd.journalq.network.command.FetchProduceFeedback;
import com.jd.journalq.network.command.FetchProduceFeedbackAck;
import com.jd.journalq.network.command.ProduceMessage;
import com.jd.journalq.network.command.ProduceMessageAck;
import com.jd.journalq.network.command.ProduceMessageCommit;
import com.jd.journalq.network.command.ProduceMessageCommitAck;
import com.jd.journalq.network.command.ProduceMessageData;
import com.jd.journalq.network.command.ProduceMessagePrepare;
import com.jd.journalq.network.command.ProduceMessagePrepareAck;
import com.jd.journalq.network.command.ProduceMessageRollback;
import com.jd.journalq.network.command.ProduceMessageRollbackAck;
import com.jd.journalq.network.command.TxStatus;
import com.jd.journalq.network.transport.TransportAttribute;
import com.jd.journalq.network.transport.command.CommandCallback;
import com.jd.journalq.network.transport.command.JMQCommand;

import java.util.Collection;
import java.util.Map;

/**
 * ProducerClient
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
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
            }
        }
        return producerClient;
    }

    public ProducerClient(Client client) {
        this.client = client;
        this.connectionState = new ProducerConnectionState(this);
    }

    public ProduceMessageAck produceMessage(String app, Map<String, ProduceMessageData> messages, long timeout) {
        ProduceMessage produceMessage = new ProduceMessage();
        produceMessage.setApp(app);
        produceMessage.setData(messages);
        return (ProduceMessageAck) client.sync(new JMQCommand(produceMessage), timeout).getPayload();
    }

    public void produceMessageOneway(String app, Map<String, ProduceMessageData> messages, long timeout) {
        ProduceMessage produceMessage = new ProduceMessage();
        produceMessage.setApp(app);
        produceMessage.setData(messages);
        client.oneway(new JMQCommand(produceMessage), timeout);
    }

    public void asyncProduceMessage(String app, Map<String, ProduceMessageData> messages, long timeout, CommandCallback callback) {
        ProduceMessage produceMessage = new ProduceMessage();
        produceMessage.setApp(app);
        produceMessage.setData(messages);
        client.async(new JMQCommand(produceMessage), timeout, callback);
    }

    public ProduceMessagePrepareAck produceMessagePrepare(String topic, String app, long sequence, String transactionId, long transactionTimeout, long timeout) {
        ProduceMessagePrepare produceMessagePrepare = new ProduceMessagePrepare();
        produceMessagePrepare.setTopic(topic);
        produceMessagePrepare.setApp(app);
        produceMessagePrepare.setSequence(sequence);
        produceMessagePrepare.setTransactionId(transactionId);
        produceMessagePrepare.setTimeout((int) transactionTimeout);
        return (ProduceMessagePrepareAck) client.sync(new JMQCommand(produceMessagePrepare), timeout).getPayload();
    }

    public ProduceMessageCommitAck produceMessageCommit(String topic, String app, String txId, long timeout) {
        ProduceMessageCommit produceMessageCommit = new ProduceMessageCommit();
        produceMessageCommit.setTopic(topic);
        produceMessageCommit.setApp(app);
        produceMessageCommit.setTxId(txId);
        return (ProduceMessageCommitAck) client.sync(new JMQCommand(produceMessageCommit), timeout).getPayload();
    }

    public ProduceMessageRollbackAck produceMessageRollback(String topic, String app, String txId, long timeout) {
        ProduceMessageRollback produceMessageRollback = new ProduceMessageRollback();
        produceMessageRollback.setTopic(topic);
        produceMessageRollback.setApp(app);
        produceMessageRollback.setTxId(txId);
        return (ProduceMessageRollbackAck) client.sync(new JMQCommand(produceMessageRollback), timeout).getPayload();
    }

    public FetchProduceFeedbackAck fetchFeedback(String topic, String app, TxStatus txStatus, int count, long longPollTimeout, long timeout) {
        FetchProduceFeedback fetchProduceFeedback = new FetchProduceFeedback();
        fetchProduceFeedback.setTopic(topic);
        fetchProduceFeedback.setApp(app);
        fetchProduceFeedback.setStatus(txStatus);
        fetchProduceFeedback.setCount(count);
        fetchProduceFeedback.setLongPollTimeout((int) longPollTimeout);
        return (FetchProduceFeedbackAck) client.sync(new JMQCommand(fetchProduceFeedback), timeout).getPayload();
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