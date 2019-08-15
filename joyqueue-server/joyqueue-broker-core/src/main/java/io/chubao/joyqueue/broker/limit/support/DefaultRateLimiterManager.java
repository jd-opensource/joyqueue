package io.chubao.joyqueue.broker.limit.support;

import io.chubao.joyqueue.broker.BrokerContext;
import io.chubao.joyqueue.broker.cluster.ClusterManager;
import io.chubao.joyqueue.broker.limit.LimitType;
import io.chubao.joyqueue.broker.limit.config.LimiterConfig;
import io.chubao.joyqueue.domain.Consumer;
import io.chubao.joyqueue.domain.Producer;
import io.chubao.joyqueue.domain.TopicName;
import io.chubao.joyqueue.event.ConsumerEvent;
import io.chubao.joyqueue.event.MetaEvent;
import io.chubao.joyqueue.event.ProducerEvent;
import io.chubao.joyqueue.event.TopicEvent;
import io.chubao.joyqueue.toolkit.concurrent.EventListener;
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