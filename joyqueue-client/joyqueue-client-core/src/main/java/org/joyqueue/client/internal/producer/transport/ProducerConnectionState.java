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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.joyqueue.network.command.AddProducerRequest;
import org.joyqueue.network.command.RemoveProducerRequest;
import org.joyqueue.network.transport.command.JoyQueueCommand;
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
 * ProducerConnectionState
 *
 * author: gaohaoxiang
 * date: 2018/12/3
 */
public class ProducerConnectionState {

    protected static final Logger logger = LoggerFactory.getLogger(ProducerConnectionState.class);

    private static final AtomicLong SEQUENCE = new AtomicLong();

    private ProducerClient producerClient;
    private ConcurrentMap<String, Set<String>> producerMap = Maps.newConcurrentMap();
    private ReentrantReadWriteLock producerLock = new ReentrantReadWriteLock();

    public ProducerConnectionState(ProducerClient producerClient) {
        this.producerClient = producerClient;
    }

    public void handleAddProducers() {
        if (MapUtils.isEmpty(producerMap)) {
            return;
        }
        try {
            for (Map.Entry<String, Set<String>> entry : producerMap.entrySet()) {
                doHandleAddProducers(Lists.newArrayList(entry.getValue()), entry.getKey());
            }
        } catch (Exception e) {
            logger.error("add producer exception, producerMap: {}", producerMap, e);
            producerMap.clear();
        }
    }

    public void handleAddProducers(Collection<String> topics, String app) {
        producerLock.readLock().lock();
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
            if (doHandleAddProducers(addTopics, app)) {
                topicSet.addAll(addTopics);
            }
        } finally {
            producerLock.readLock().unlock();
        }
    }

    public void handleRemoveProducers(Collection<String> topics, String app) {
        producerLock.writeLock().lock();
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
            if (doHandleRemoveProducers(removeTopics, app)) {
                topicSet.removeAll(removeTopics);
            }
        } finally {
            producerLock.writeLock().unlock();
        }
    }

    public void handleRemoveProducers() {
        if (MapUtils.isEmpty(producerMap)) {
            return;
        }
        for (Map.Entry<String, Set<String>> entry : producerMap.entrySet()) {
            doHandleRemoveProducers(Lists.newArrayList(entry.getValue()), entry.getKey());
        }
    }

    protected boolean doHandleAddProducers(List<String> topics, String app) {
        AddProducerRequest addProducerRequest = new AddProducerRequest();
        addProducerRequest.setTopics(topics);
        addProducerRequest.setApp(app);
        addProducerRequest.setSequence(SEQUENCE.incrementAndGet());
        try {
            producerClient.getClient().sync(new JoyQueueCommand(addProducerRequest));
            return true;
        } catch (Exception e) {
            logger.warn("add producer exception, topics: {}, app: {}, error: {}", topics, app, e.getMessage());
            logger.debug("add producer exception, topics: {}, app: {}", topics, app, e);
            return false;
        }
    }

    protected boolean doHandleRemoveProducers(List<String> topics, String app) {
        RemoveProducerRequest removeProducerRequest = new RemoveProducerRequest();
        removeProducerRequest.setTopics(topics);
        removeProducerRequest.setApp(app);
        try {
            producerClient.getClient().sync(new JoyQueueCommand(removeProducerRequest));
            return true;
        } catch (Exception e) {
            logger.warn("remove producer exception, topics: {}, app: {}, error: {}", topics, app, e.getMessage());
            logger.debug("remove producer exception, topics: {}, app: {}", topics, app, e);
            return false;
        }
    }

    protected Set<String> getOrCreateAddedTopicSet(String app) {
        Set<String> topicSet = producerMap.get(app);
        if (topicSet == null) {
            topicSet = Sets.newConcurrentHashSet();
            Set<String> oldTopicSet = producerMap.putIfAbsent(app, topicSet);
            if (oldTopicSet != null) {
                topicSet = oldTopicSet;
            }
        }
        return topicSet;
    }
}