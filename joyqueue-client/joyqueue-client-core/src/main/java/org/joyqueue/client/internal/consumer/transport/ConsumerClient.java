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
package org.joyqueue.client.internal.consumer.transport;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Maps;
import com.google.common.collect.Table;
import org.joyqueue.client.internal.transport.Client;
import org.joyqueue.client.internal.transport.ClientState;
import org.joyqueue.network.command.CommitAckData;
import org.joyqueue.network.command.CommitAckRequest;
import org.joyqueue.network.command.CommitAckResponse;
import org.joyqueue.network.command.CommitIndexRequest;
import org.joyqueue.network.command.CommitIndexResponse;
import org.joyqueue.network.command.FetchIndexRequest;
import org.joyqueue.network.command.FetchIndexResponse;
import org.joyqueue.network.command.FetchPartitionMessageData;
import org.joyqueue.network.command.FetchPartitionMessageRequest;
import org.joyqueue.network.command.FetchPartitionMessageResponse;
import org.joyqueue.network.command.FetchTopicMessageData;
import org.joyqueue.network.command.FetchTopicMessageRequest;
import org.joyqueue.network.command.FetchTopicMessageResponse;
import org.joyqueue.network.transport.TransportAttribute;
import org.joyqueue.network.transport.command.Command;
import org.joyqueue.network.transport.command.CommandCallback;
import org.joyqueue.network.transport.command.JoyQueueCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * ConsumerClient
 *
 * author: gaohaoxiang
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
            } else {
                consumerClient.getClient().addListener(new ConsumerClientConnectionListener(consumerClient.getClient().getTransport(), consumerClient));
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

    public CommitIndexResponse commitIndex(Table<String, Short, Long> partitions, String app, long timeout) {
        CommitIndexRequest commitIndexRequest = buildCommitIndexCommand(partitions, app);
        return (CommitIndexResponse) client.sync(new JoyQueueCommand(commitIndexRequest), timeout).getPayload();
    }

    public void addConsumers() {
        connectionState.handleAddConsumers();
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

    protected CommitIndexRequest buildCommitIndexCommand(Table<String, Short, Long> partitions, String app) {
        CommitIndexRequest commitIndexRequest = new CommitIndexRequest();
        commitIndexRequest.setData(partitions);
        commitIndexRequest.setApp(app);
        return commitIndexRequest;
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