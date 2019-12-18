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
package io.chubao.joyqueue.broker.limit.support;

import io.chubao.joyqueue.broker.BrokerContext;
import io.chubao.joyqueue.broker.cluster.ClusterManager;
import io.chubao.joyqueue.broker.limit.LimitType;
import io.chubao.joyqueue.broker.limit.config.LimiterConfig;
import io.chubao.joyqueue.domain.Consumer;
import io.chubao.joyqueue.domain.Producer;
import io.chubao.joyqueue.domain.TopicName;
import io.chubao.joyqueue.event.MetaEvent;
import io.chubao.joyqueue.nsr.event.AddConsumerEvent;
import io.chubao.joyqueue.nsr.event.AddProducerEvent;
import io.chubao.joyqueue.nsr.event.AddTopicEvent;
import io.chubao.joyqueue.nsr.event.RemoveConsumerEvent;
import io.chubao.joyqueue.nsr.event.RemoveProducerEvent;
import io.chubao.joyqueue.nsr.event.RemoveTopicEvent;
import io.chubao.joyqueue.nsr.event.UpdateConsumerEvent;
import io.chubao.joyqueue.nsr.event.UpdateProducerEvent;
import io.chubao.joyqueue.nsr.event.UpdateTopicEvent;
import io.chubao.joyqueue.toolkit.concurrent.EventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DefaultRateLimiterManager
 *
 * author: gaohaoxiang
 * date: 2019/5/17
 */
public class DefaultRateLimiterManager extends AbstractRateLimiterManager implements EventListener<MetaEvent> {

    protected static final Logger logger = LoggerFactory.getLogger(DefaultRateLimiterManager.class);

    private ClusterManager clusterManager;

    public DefaultRateLimiterManager(BrokerContext brokerContext) {
        this.clusterManager = brokerContext.getClusterManager();
        this.clusterManager.addListener(this);
    }

    @Override
    protected LimiterConfig getLimiterConfig(String topic, String app, String type) {
        Integer tps = null;
        Integer traffic = null;

        if (LimitType.FETCH.getType().equals(type)) {
            Consumer consumer = clusterManager.tryGetConsumer(TopicName.parse(topic), app);
            if (consumer != null && consumer.getLimitPolicy() != null) {
                tps = consumer.getLimitPolicy().getTps();
                traffic = consumer.getLimitPolicy().getTraffic();
            }
        } else if (LimitType.PRODUCE.getType().equals(type)) {
            Producer producer = clusterManager.tryGetProducer(TopicName.parse(topic), app);
            if (producer != null && producer.getLimitPolicy() != null) {
                tps = producer.getLimitPolicy().getTps();
                traffic = producer.getLimitPolicy().getTraffic();
            }
        } else {
            logger.warn("unsupported limit type, topic: {}, app: {}, type: {}", topic, app, type);
            return null;
        }

        if ((tps == null && traffic == null) || (tps <= 0 && traffic <= 0)) {
            return null;
        }

        if (tps <= 0) {
            tps = Integer.MAX_VALUE;
        }
        if (traffic <= 0) {
            traffic = Integer.MAX_VALUE;
        }
        return new LimiterConfig(tps, traffic);
    }

    @Override
    public void onEvent(MetaEvent event) {
        switch (event.getEventType()) {
            case ADD_CONSUMER: {
                AddConsumerEvent addConsumerEvent = (AddConsumerEvent) event;
                removeAppRateLimiter(addConsumerEvent.getTopic().getFullName(), addConsumerEvent.getConsumer().getApp());
                break;
            }
            case UPDATE_CONSUMER: {
                UpdateConsumerEvent updateConsumerEvent = (UpdateConsumerEvent) event;
                removeAppRateLimiter(updateConsumerEvent.getTopic().getFullName(), updateConsumerEvent.getNewConsumer().getApp());
                break;
            }
            case REMOVE_CONSUMER: {
                RemoveConsumerEvent removeConsumerEvent = (RemoveConsumerEvent) event;
                removeAppRateLimiter(removeConsumerEvent.getTopic().getFullName(), removeConsumerEvent.getConsumer().getApp());
                break;
            }
            case ADD_PRODUCER: {
                AddProducerEvent addProducerEvent = (AddProducerEvent) event;
                removeAppRateLimiter(addProducerEvent.getTopic().getFullName(), addProducerEvent.getProducer().getApp());
                break;
            }
            case UPDATE_PRODUCER: {
                UpdateProducerEvent updateProducerEvent = (UpdateProducerEvent) event;
                removeAppRateLimiter(updateProducerEvent.getTopic().getFullName(), updateProducerEvent.getNewProducer().getApp());
                break;
            }
            case REMOVE_PRODUCER: {
                RemoveProducerEvent removeProducerEvent = (RemoveProducerEvent) event;
                removeAppRateLimiter(removeProducerEvent.getTopic().getFullName(), removeProducerEvent.getProducer().getApp());
                break;
            }
            case ADD_TOPIC: {
                AddTopicEvent addTopicEvent = (AddTopicEvent) event;
                removeTopicRateLimiter(addTopicEvent.getTopic().getName().getFullName());
                break;
            }
            case UPDATE_TOPIC: {
                UpdateTopicEvent updateTopicEvent = (UpdateTopicEvent) event;
                removeTopicRateLimiter(updateTopicEvent.getNewTopic().getName().getFullName());
                break;
            }
            case REMOVE_TOPIC: {
                RemoveTopicEvent topicEvent = (RemoveTopicEvent) event;
                removeTopicRateLimiter(topicEvent.getTopic().getName().getFullName());
                break;
            }
        }
    }
}