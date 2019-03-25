package com.jd.journalq.client.internal.consumer.transport;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Maps;
import com.google.common.collect.Table;
import com.jd.journalq.client.internal.transport.Client;
import com.jd.journalq.client.internal.transport.ClientState;
import com.jd.journalq.common.network.command.CommitAck;
import com.jd.journalq.common.network.command.CommitAckAck;
import com.jd.journalq.common.network.command.CommitAckData;
import com.jd.journalq.common.network.command.FetchIndex;
import com.jd.journalq.common.network.command.FetchIndexAck;
import com.jd.journalq.common.network.command.FetchPartitionMessage;
import com.jd.journalq.common.network.command.FetchPartitionMessageAck;
import com.jd.journalq.common.network.command.FetchPartitionMessageData;
import com.jd.journalq.common.network.command.FetchTopicMessage;
import com.jd.journalq.common.network.command.FetchTopicMessageAck;
import com.jd.journalq.common.network.command.FetchTopicMessageData;
import com.jd.journalq.common.network.transport.TransportAttribute;
import com.jd.journalq.common.network.transport.command.Command;
import com.jd.journalq.common.network.transport.command.CommandCallback;
import com.jd.journalq.common.network.transport.command.JMQCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * ConsumerClient
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/11/29
 */
public class ConsumerClient {

    private static final String CLIENT_CONSUMER_CACHE_KEY = "_CLIENT_CONSUMER_CACHE_";

    protected static final Logger logger = LoggerFactory.getLogger(ConsumerClient.class);

    private Client client;

    private ConsumerConnectionState connectionState;

    public static ConsumerClient build(Client client) {
        ConsumerClient consumerClient = client.getAttribute().get(CLIENT_CONSUMER_CACHE_KEY);
        if (consumerClient == null) {
            consumerClient = new ConsumerClient(client);
            ConsumerClient oldConsumerClient = client.getAttribute().putIfAbsent(CLIENT_CONSUMER_CACHE_KEY, consumerClient);
            if (oldConsumerClient != null) {
                consumerClient = oldConsumerClient;
            }
        }
        return consumerClient;
    }

    public ConsumerClient(Client client) {
        this.client = client;
        this.connectionState = new ConsumerConnectionState(this);
    }

    public FetchIndexAck fetchIndex(Map<String, List<Short>> partitions, String app, long timeout) {
        FetchIndex fetchIndex = new FetchIndex();
        fetchIndex.setPartitions(partitions);
        fetchIndex.setApp(app);
        return (FetchIndexAck) client.sync(new JMQCommand(fetchIndex), timeout).getPayload();
    }

    public CommitAckAck commitAck(Table<String, Short, List<CommitAckData>> data, String app, long timeout) {
        CommitAck commitAck = new CommitAck();
        commitAck.setData(data);
        commitAck.setApp(app);
        return (CommitAckAck) client.sync(new JMQCommand(commitAck), timeout).getPayload();
    }

    public void asyncFetchTopicMessage(List<String> topics, String app, int count, long timeout, long ackTimeout, long longPollTimeout, CommandCallback callback) {
        FetchTopicMessage fetchTopicMessage = buildFetchTopicMessageCommand(topics, app, count, ackTimeout, longPollTimeout);
        client.async(new JMQCommand(fetchTopicMessage), timeout, callback);
    }

    public FetchTopicMessageAck fetchTopicMessage(List<String> topics, String app, int count, long timeout, long ackTimeout, long longPollTimeout) {
        FetchTopicMessage fetchTopicMessage = buildFetchTopicMessageCommand(topics, app, count, ackTimeout, longPollTimeout);
        Command response = client.sync(new JMQCommand(fetchTopicMessage), timeout);
        return (FetchTopicMessageAck) response.getPayload();
    }

    public FetchPartitionMessageAck fetchPartitionMessage(Map<String, Short> partitions, String app, int count, long timeout) {
        FetchPartitionMessage fetchPartitionMessage = buildPartitionTopicMessageCommand(partitions, app, count);
        Command response = client.sync(new JMQCommand(fetchPartitionMessage), timeout);
        return (FetchPartitionMessageAck) response.getPayload();
    }

    public void asyncFetchPartitionMessage(Map<String, Short> partitions, String app, int count, long timeout, CommandCallback callback) {
        FetchPartitionMessage fetchPartitionMessage = buildPartitionTopicMessageCommand(partitions, app, count);
        client.async(new JMQCommand(fetchPartitionMessage), timeout, callback);
    }

    public FetchPartitionMessageAck fetchPartitionMessage(Table<String, Short, Long> partitions, String app, int count, long timeout) {
        FetchPartitionMessage fetchPartitionMessage = buildPartitionTopicMessageCommand(partitions, app, count);
        Command response = client.sync(new JMQCommand(fetchPartitionMessage), timeout);
        return (FetchPartitionMessageAck) response.getPayload();
    }

    public void asyncFetchPartitionMessage(Table<String, Short, Long> partitions, String app, int count, long timeout, CommandCallback callback) {
        FetchPartitionMessage fetchPartitionMessage = buildPartitionTopicMessageCommand(partitions, app, count);
        client.async(new JMQCommand(fetchPartitionMessage), timeout, callback);
    }

    public void addConsumers(Collection<String> topics, String app) {
        connectionState.handleAddConsumers(topics, app);
    }

    public void removeConsumers(Collection<String> topics, String app) {
        connectionState.handleRemoveConsumers(topics, app);
    }

    public void close() {
        connectionState.handleRemoveConsumers();
        client.stop();
    }

    protected FetchPartitionMessage buildPartitionTopicMessageCommand(Map<String, Short> partitions, String app, int count) {
        Table<String, Short, FetchPartitionMessageData> partitionMap = HashBasedTable.create();
        for (Map.Entry<String, Short> entry : partitions.entrySet()) {
            partitionMap.put(entry.getKey(), entry.getValue(), new FetchPartitionMessageData(count, FetchPartitionMessage.NONE_INDEX));
        }

        FetchPartitionMessage fetchPartitionMessage = new FetchPartitionMessage();
        fetchPartitionMessage.setPartitions(partitionMap);
        fetchPartitionMessage.setApp(app);
        return fetchPartitionMessage;
    }

    protected FetchPartitionMessage buildPartitionTopicMessageCommand(Table<String, Short, Long> partitions, String app, int count) {
        Table<String, Short, FetchPartitionMessageData> partitionMap = HashBasedTable.create();
        for (Map.Entry<String, Map<Short, Long>> topicEntry : partitions.rowMap().entrySet()) {
            String topic = topicEntry.getKey();
            for (Map.Entry<Short, Long> partitionEntry : topicEntry.getValue().entrySet()) {
                partitionMap.put(topic, partitionEntry.getKey(), new FetchPartitionMessageData(count, partitionEntry.getValue()));
            }
        }

        FetchPartitionMessage fetchPartitionMessage = new FetchPartitionMessage();
        fetchPartitionMessage.setPartitions(partitionMap);
        fetchPartitionMessage.setApp(app);
        return fetchPartitionMessage;
    }

    protected FetchTopicMessage buildFetchTopicMessageCommand(List<String> topics, String app, int count, long ackTimeout, long longPollTimeout) {
        Map<String, FetchTopicMessageData> topicMap = Maps.newHashMap();
        for (String topic : topics) {
            topicMap.put(topic, new FetchTopicMessageData(count));
        }

        FetchTopicMessage fetchTopicMessage = new FetchTopicMessage();
        fetchTopicMessage.setTopics(topicMap);
        fetchTopicMessage.setApp(app);
        fetchTopicMessage.setAckTimeout((int) ackTimeout);
        fetchTopicMessage.setLongPollTimeout((int) longPollTimeout);
        return fetchTopicMessage;
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