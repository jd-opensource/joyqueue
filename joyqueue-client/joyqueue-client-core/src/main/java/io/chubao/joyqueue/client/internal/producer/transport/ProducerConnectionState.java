package io.chubao.joyqueue.client.internal.producer.transport;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import io.chubao.joyqueue.network.command.AddProducerRequest;
import io.chubao.joyqueue.network.command.RemoveProducerRequest;
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
 * ProducerConnectionState
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/3
 */
public class ProducerConnectionState {

    protected static final Logger logger = LoggerFactory.getLogger(ProducerConnectionState.class);

    private static final String ADDED_PRODUCER_KEY = "_CLIENT_ADDED_PRODUCER_";
    private static final AtomicLong SEQUENCE = new AtomicLong();

    private ProducerClient producerClient;
    private ReentrantReadWriteLock producerLock = new ReentrantReadWriteLock();

    public ProducerConnectionState(ProducerClient producerClient) {
        this.producerClient = producerClient;
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
        ConcurrentMap<String, Set<String>> appTopicMap = getOrCreateAddedTopicMap();
        if (MapUtils.isEmpty(appTopicMap)) {
            return;
        }
        for (Map.Entry<String, Set<String>> entry : appTopicMap.entrySet()) {
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

    protected ConcurrentMap<String, Set<String>> getOrCreateAddedTopicMap() {
        TransportAttribute attribute = producerClient.getAttribute();
        return attribute.get(ADDED_PRODUCER_KEY);
    }

    protected Set<String> getOrCreateAddedTopicSet(String app) {
        TransportAttribute attribute = producerClient.getAttribute();
        ConcurrentMap<String, Set<String>> appMap = attribute.get(ADDED_PRODUCER_KEY);

        if (appMap == null) {
            appMap = Maps.newConcurrentMap();
            ConcurrentMap<String, Set<String>> oldAppMap = attribute.putIfAbsent(ADDED_PRODUCER_KEY, appMap);
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