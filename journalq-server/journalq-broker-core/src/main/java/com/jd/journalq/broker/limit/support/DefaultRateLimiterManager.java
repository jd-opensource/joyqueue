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
package com.jd.journalq.broker.limit.support;

import com.jd.journalq.broker.BrokerContext;
import com.jd.journalq.broker.cluster.ClusterManager;
import com.jd.journalq.broker.limit.LimitType;
import com.jd.journalq.broker.limit.config.LimiterConfig;
import com.jd.journalq.domain.Consumer;
import com.jd.journalq.domain.Producer;
import com.jd.journalq.domain.TopicName;
import com.jd.journalq.event.ConsumerEvent;
import com.jd.journalq.event.MetaEvent;
import com.jd.journalq.event.ProducerEvent;
import com.jd.journalq.event.TopicEvent;
import com.jd.journalq.toolkit.concurrent.EventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DefaultRateLimiterManager
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
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
            case ADD_CONSUMER:
            case UPDATE_CONSUMER:
            case REMOVE_CONSUMER: {
                ConsumerEvent consumerEvent = (ConsumerEvent) event;
                removeAppRateLimiter(consumerEvent.getTopic().getFullName(), consumerEvent.getApp());
            }
            case ADD_PRODUCER:
            case UPDATE_PRODUCER:
            case REMOVE_PRODUCER: {
                ProducerEvent producerEvent = (ProducerEvent) event;
                removeAppRateLimiter(producerEvent.getTopic().getFullName(), producerEvent.getApp());
            }
            case ADD_TOPIC:
            case UPDATE_TOPIC:
            case REMOVE_TOPIC: {
                TopicEvent topicEvent = (TopicEvent) event;
                removeTopicRateLimiter(topicEvent.getTopic().getFullName());
            }
        }
    }
}