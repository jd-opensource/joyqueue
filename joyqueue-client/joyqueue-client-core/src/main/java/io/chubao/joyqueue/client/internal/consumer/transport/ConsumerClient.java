package io.chubao.joyqueue.client.internal.consumer.transport;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Maps;
import com.google.common.collect.Table;
import io.chubao.joyqueue.client.internal.transport.Client;
import io.chubao.joyqueue.client.internal.transport.ClientState;
import io.chubao.joyqueue.network.command.CommitAckRequest;
import io.chubao.joyqueue.network.command.CommitAckResponse;
import io.chubao.joyqueue.network.command.CommitAckData;
import io.chubao.joyqueue.network.command.FetchIndexRequest;
import io.chubao.joyqueue.network.command.FetchIndexResponse;
import io.chubao.joyqueue.network.command.FetchPartitionMessageRequest;
import io.chubao.joyqueue.network.command.FetchPartitionMessageResponse;
import io.chubao.joyqueue.network.command.FetchPartitionMessageData;
import io.chubao.joyqueue.network.command.FetchTopicMessageRequest;
import io.chubao.joyqueue.network.command.FetchTopicMessageResponse;
import io.chubao.joyqueue.network.command.FetchTopicMessageData;
import io.chubao.joyqueue.network.transport.TransportAttribute;
import io.chubao.joyqueue.network.transport.command.Command;
import io.chubao.joyqueue.network.transport.command.CommandCallback;
import io.chubao.joyqueue.network.transport.command.JoyQueueCommand;
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

    public FetchIndexResponse fetchIndex(Map<String, List<Short>> partitions, String app, long timeout) {
        FetchIndexRequest fetchIndexRequest = new FetchIndexRequest();
        fetchIndexRequest.setPartitions(partitions);
        fetchIndexRequest.setApp(app);
        return (FetchIndexResponse) client.sync(new JoyQueueCommand(fetchIndexRequest), timeout).getPayload();
    }

    public CommitAckResponse commitAck(Table<String, Short, List<CommitAckData>> data, String app, long timeout) {
        CommitAckRequest commitAckRequest = new CommitAckRequest();
        commitAckRequest.setData(data);
        commitAckRequest.setApp(app);
        return (CommitAckResponse) client.sync(new JoyQueueCommand(commitAckRequest), timeout).getPayload();
    }

    public void asyncFetchTopicMessage(List<String> topics, String app, int count, long timeout, long ackTimeout, long longPollTimeout, CommandCallback callback) {
        FetchTopicMessageRequest fetchTopicMessageRequest = buildFetchTopicMessageCommand(topics, app, count, ackTimeout, longPollTimeout);
        client.async(new JoyQueueCommand(fetchTopicMessageRequest), timeout, callback);
    }

    public FetchTopicMessageResponse fetchTopicMessage(List<String> topics, String app, int count, long timeout, long ackTimeout, long longPollTimeout) {
        FetchTopicMessageRequest fetchTopicMessageRequest = buildFetchTopicMessageCommand(topics, app, count, ackTimeout, longPollTimeout);
        Command response = client.sync(new JoyQueueCommand(fetchTopicMessageRequest), timeout);
        return (FetchTopicMessageResponse) response.getPayload();
    }

    public FetchPartitionMessageResponse fetchPartitionMessage(Map<String, Short> partitions, String app, int count, long timeout) {
        FetchPartitionMessageRequest fetchPartitionMessageRequest = buildPartitionTopicMessageCommand(partitions, app, count);
        Command response = client.sync(new JoyQueueCommand(fetchPartitionMessageRequest), timeout);
        return (FetchPartitionMessageResponse) response.getPayload();
    }

    public void asyncFetchPartitionMessage(Map<String, Short> partitions, String app, int count, long timeout, CommandCallback callback) {
        FetchPartitionMessageRequest fetchPartitionMessageRequest = buildPartitionTopicMessageCommand(partitions, app, count);
        client.async(new JoyQueueCommand(fetchPartitionMessageRequest), timeout, callback);
    }

    public FetchPartitionMessageResponse fetchPartitionMessage(Table<String, Short, Long> partitions, String app, int count, long timeout) {
        FetchPartitionMessageRequest fetchPartitionMessageRequest = buildPartitionTopicMessageCommand(partitions, app, count);
        Command response = client.sync(new JoyQueueCommand(fetchPartitionMessageRequest), timeout);
        return (FetchPartitionMessageResponse) response.getPayload();
    }

    public void asyncFetchPartitionMessage(Table<String, Short, Long> partitions, String app, int count, long timeout, CommandCallback callback) {
        FetchPartitionMessageRequest fetchPartitionMessageRequest = buildPartitionTopicMessageCommand(partitions, app, count);
        client.async(new JoyQueueCommand(fetchPartitionMessageRequest), timeout, callback);
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

    protected FetchPartitionMessageRequest buildPartitionTopicMessageCommand(Map<String, Short> partitions, String app, int count) {
        Table<String, Short, FetchPartitionMessageData> partitionMap = HashBasedTable.create();
        for (Map.Entry<String, Short> entry : partitions.entrySet()) {
            partitionMap.put(entry.getKey(), entry.getValue(), new FetchPartitionMessageData(count, FetchPartitionMessageRequest.NONE_INDEX));
        }

        FetchPartitionMessageRequest fetchPartitionMessageRequest = new FetchPartitionMessageRequest();
        fetchPartitionMessageRequest.setPartitions(partitionMap);
        fetchPartitionMessageRequest.setApp(app);
        return fetchPartitionMessageRequest;
    }

    protected FetchPartitionMessageRequest buildPartitionTopicMessageCommand(Table<String, Short, Long> partitions, String app, int count) {
        Table<String, Short, FetchPartitionMessageData> partitionMap = HashBasedTable.create();
        for (Map.Entry<String, Map<Short, Long>> topicEntry : partitions.rowMap().entrySet()) {
            String topic = topicEntry.getKey();
            for (Map.Entry<Short, Long> partitionEntry : topicEntry.getValue().entrySet()) {
                partitionMap.put(topic, partitionEntry.getKey(), new FetchPartitionMessageData(count, partitionEntry.getValue()));
            }
        }

        FetchPartitionMessageRequest fetchPartitionMessageRequest = new FetchPartitionMessageRequest();
        fetchPartitionMessageRequest.setPartitions(partitionMap);
        fetchPartitionMessageRequest.setApp(app);
        return fetchPartitionMessageRequest;
    }

    protected FetchTopicMessageRequest buildFetchTopicMessageCommand(List<String> topics, String app, int count, long ackTimeout, long longPollTimeout) {
        Map<String, FetchTopicMessageData> topicMap = Maps.newHashMap();
        for (String topic : topics) {
            topicMap.put(topic, new FetchTopicMessageData(count));
        }

        FetchTopicMessageRequest fetchTopicMessageRequest = new FetchTopicMessageRequest();
        fetchTopicMessageRequest.setTopics(topicMap);
        fetchTopicMessageRequest.setApp(app);
        fetchTopicMessageRequest.setAckTimeout((int) ackTimeout);
        fetchTopicMessageRequest.setLongPollTimeout((int) longPollTimeout);
        return fetchTopicMessageRequest;
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