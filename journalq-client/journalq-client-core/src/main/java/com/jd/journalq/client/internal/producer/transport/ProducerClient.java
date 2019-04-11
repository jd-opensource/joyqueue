package com.jd.journalq.client.internal.producer.transport;

import com.jd.journalq.client.internal.transport.Client;
import com.jd.journalq.client.internal.transport.ClientState;
import com.jd.journalq.network.command.FetchProduceFeedbackRequest;
import com.jd.journalq.network.command.FetchProduceFeedbackResponse;
import com.jd.journalq.network.command.ProduceMessageCommitRequest;
import com.jd.journalq.network.command.ProduceMessagePrepareRequest;
import com.jd.journalq.network.command.ProduceMessagePrepareResponse;
import com.jd.journalq.network.command.ProduceMessageRequest;
import com.jd.journalq.network.command.ProduceMessageResponse;
import com.jd.journalq.network.command.ProduceMessageCommitAck;
import com.jd.journalq.network.command.ProduceMessageData;
import com.jd.journalq.network.command.ProduceMessageRollbackRequest;
import com.jd.journalq.network.command.ProduceMessageRollbackResponse;
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

    public ProduceMessageResponse produceMessage(String app, Map<String, ProduceMessageData> messages, long timeout) {
        ProduceMessageRequest produceMessageRequest = new ProduceMessageRequest();
        produceMessageRequest.setApp(app);
        produceMessageRequest.setData(messages);
        return (ProduceMessageResponse) client.sync(new JMQCommand(produceMessageRequest), timeout).getPayload();
    }

    public void produceMessageOneway(String app, Map<String, ProduceMessageData> messages, long timeout) {
        ProduceMessageRequest produceMessageRequest = new ProduceMessageRequest();
        produceMessageRequest.setApp(app);
        produceMessageRequest.setData(messages);
        client.oneway(new JMQCommand(produceMessageRequest), timeout);
    }

    public void asyncProduceMessage(String app, Map<String, ProduceMessageData> messages, long timeout, CommandCallback callback) {
        ProduceMessageRequest produceMessageRequest = new ProduceMessageRequest();
        produceMessageRequest.setApp(app);
        produceMessageRequest.setData(messages);
        client.async(new JMQCommand(produceMessageRequest), timeout, callback);
    }

    public ProduceMessagePrepareResponse produceMessagePrepare(String topic, String app, long sequence, String transactionId, long transactionTimeout, long timeout) {
        ProduceMessagePrepareRequest produceMessagePrepareRequest = new ProduceMessagePrepareRequest();
        produceMessagePrepareRequest.setTopic(topic);
        produceMessagePrepareRequest.setApp(app);
        produceMessagePrepareRequest.setSequence(sequence);
        produceMessagePrepareRequest.setTransactionId(transactionId);
        produceMessagePrepareRequest.setTimeout((int) transactionTimeout);
        return (ProduceMessagePrepareResponse) client.sync(new JMQCommand(produceMessagePrepareRequest), timeout).getPayload();
    }

    public ProduceMessageCommitAck produceMessageCommit(String topic, String app, String txId, long timeout) {
        ProduceMessageCommitRequest produceMessageCommitRequest = new ProduceMessageCommitRequest();
        produceMessageCommitRequest.setTopic(topic);
        produceMessageCommitRequest.setApp(app);
        produceMessageCommitRequest.setTxId(txId);
        return (ProduceMessageCommitAck) client.sync(new JMQCommand(produceMessageCommitRequest), timeout).getPayload();
    }

    public ProduceMessageRollbackResponse produceMessageRollback(String topic, String app, String txId, long timeout) {
        ProduceMessageRollbackRequest produceMessageRollbackRequest = new ProduceMessageRollbackRequest();
        produceMessageRollbackRequest.setTopic(topic);
        produceMessageRollbackRequest.setApp(app);
        produceMessageRollbackRequest.setTxId(txId);
        return (ProduceMessageRollbackResponse) client.sync(new JMQCommand(produceMessageRollbackRequest), timeout).getPayload();
    }

    public FetchProduceFeedbackResponse fetchFeedback(String topic, String app, TxStatus txStatus, int count, long longPollTimeout, long timeout) {
        FetchProduceFeedbackRequest fetchProduceFeedbackRequest = new FetchProduceFeedbackRequest();
        fetchProduceFeedbackRequest.setTopic(topic);
        fetchProduceFeedbackRequest.setApp(app);
        fetchProduceFeedbackRequest.setStatus(txStatus);
        fetchProduceFeedbackRequest.setCount(count);
        fetchProduceFeedbackRequest.setLongPollTimeout((int) longPollTimeout);
        return (FetchProduceFeedbackResponse) client.sync(new JMQCommand(fetchProduceFeedbackRequest), timeout).getPayload();
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