/**
 * Copyright 2018 The JoyQueue Authors.
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
package io.chubao.joyqueue.client.internal.consumer.transport;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import io.chubao.joyqueue.network.command.AddConsumerRequest;
import io.chubao.joyqueue.network.command.RemoveConsumerRequest;
import io.chubao.joyqueue.network.transport.TransportAttribute;
import io.chubao.joyqueue.network.transport.command.JoyQueueCommand;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * ConsumerConnectionState
 *
 * author: gaohaoxiang
 * date: 2018/12/3
 */
public class ConsumerConnectionState {

    protected static final Logger logger = LoggerFactory.getLogger(ConsumerConnectionState.class);

    private static final String ADDED_CONSUMER_KEY = "_CLIENT_ADDED_CONSUMER_";

    private static final AtomicLong SEQUENCE = new AtomicLong();

    private ConsumerClient consumerClient;
    private ReentrantReadWriteLock consumerLock = new ReentrantReadWriteLock();

    public ConsumerConnectionState(ConsumerClient consumerClient) {
        this.consumerClient = consumerClient;
    }

    public void handleAddConsumers(Collection<String> topics, String app) {
        consumerLock.readLock().lock();
        try {
            Set<String> topicSet = getOrCreateAddedTopicSet(app);
            List<String> addTopics = null;

            for (String topic : topics) {
                if (!topicSet.contains(topic)) {
                    if (addTopics == null) {
                        addTopics = Lists.newLinkedList();
                    }
                    addTopics.add(topic);
                }
            }

            if (CollectionUtils.isEmpty(addTopics)) {
                return;
            }
            if (doHandleAddConsumers(addTopics, app)) {
                topicSet.addAll(addTopics);
            }
        } finally {
            consumerLock.readLock().unlock();
        }
    }

    public void handleRemoveConsumers(Collection<String> topics, String app) {
        consumerLock.writeLock().lock();
        try {
            Set<String> topicSet = getOrCreateAddedTopicSet(app);
            List<String> removeTopics = null;

            for (String topic : topics) {
                if (topicSet.contains(topic)) {
                    if (removeTopics == null) {
                        removeTopics = Lists.newLinkedList();
                    }
                    removeTopics.add(topic);
                }
            }

            if (CollectionUtils.isEmpty(removeTopics)) {
                return;
            }
            if (doHandleRemoveConsumers(removeTopics, app)) {
                topicSet.removeAll(removeTopics);
            }
        } finally {
            consumerLock.writeLock().unlock();
        }
    }

    public void handleRemoveConsumers() {
        ConcurrentMap<String, Set<String>> appTopicSet = getOrCreateAddedTopicSet();
        if (MapUtils.isEmpty(appTopicSet)) {
            return;
        }
        for (Map.Entry<String, Set<String>> entry : appTopicSet.entrySet()) {
            doHandleRemoveConsumers(Lists.newArrayList(entry.getValue()), entry.getKey());
        }
    }

    protected boolean doHandleAddConsumers(List<String> topics, String app) {
        AddConsumerRequest addConsumerRequest = new AddConsumerRequest();
        addConsumerRequest.setTopics(topics);
        addConsumerRequest.setApp(app);
        addConsumerRequest.setSequence(SEQUENCE.incrementAndGet());
        try {
            consumerClient.getClient().sync(new JoyQueueCommand(addConsumerRequest));
            return true;
        } catch (Exception e) {
            logger.warn("add consumer exception, topics: {}, app: {}, error: {}", topics, app, e.getMessage());
            logger.debug("add consumer exception, topics: {}, app: {}", topics, app, e);
            return false;
        }
    }

    protected boolean doHandleRemoveConsumers(List<String> topics, String app) {
        RemoveConsumerRequest removeConsumerRequest = new RemoveConsumerRequest();
        removeConsumerRequest.setTopics(topics);
        removeConsumerRequest.setApp(app);
        try {
            consumerClient.getClient().sync(new JoyQueueCommand(removeConsumerRequest));
            return true;
        } catch (Exception e) {
            logger.warn("remove consumer exception, topics: {}, app: {}, error: {}", topics, app, e.getMessage());
            logger.debug("remove consumer exception, topics: {}, app: {}", topics, app, e);
            return false;
        }
    }

    protected ConcurrentMap<String, Set<String>> getOrCreateAddedTopicSet() {
        TransportAttribute attribute = consumerClient.getAttribute();
        return attribute.get(ADDED_CONSUMER_KEY);
    }

    protected Set<String> getOrCreateAddedTopicSet(String app) {
        TransportAttribute attribute = consumerClient.getAttribute();
        ConcurrentMap<String, Set<String>> appMap = attribute.get(ADDED_CONSUMER_KEY);

        if (appMap == null) {
            appMap = Maps.newConcurrentMap();
            ConcurrentMap<String, Set<String>> oldAppMap = attribute.putIfAbsent(ADDED_CONSUMER_KEY, appMap);
            if (oldAppMap != null) {
                appMap = oldAppMap;
            }
        }

        Set<String> topicSet = appMap.get(app);
        if (topicSet == null) {
            topicSet = Sets.newConcurrentHashSet();
            Set<String> oldTopicSet = appMap.putIfAbsent(app, topicSet);
            if (oldTopicSet != null) {
                topicSet = oldTopicSet;
            }
        }

        return topicSet;
    }
}