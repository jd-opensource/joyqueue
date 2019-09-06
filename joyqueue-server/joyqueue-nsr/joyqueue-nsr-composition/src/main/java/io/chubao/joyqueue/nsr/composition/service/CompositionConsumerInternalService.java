package io.chubao.joyqueue.nsr.composition.service;

import io.chubao.joyqueue.domain.Consumer;
import io.chubao.joyqueue.domain.TopicName;
import io.chubao.joyqueue.nsr.composition.config.CompositionConfig;
import io.chubao.joyqueue.nsr.service.internal.ConsumerInternalService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * CompositionConsumerInternalService
 * author: gaohaoxiang
 * date: 2019/8/12
 */
public class CompositionConsumerInternalService implements ConsumerInternalService {

    protected static final Logger logger = LoggerFactory.getLogger(CompositionConsumerInternalService.class);

    private CompositionConfig config;
    private ConsumerInternalService igniteConsumerService;
    private ConsumerInternalService journalkeeperConsumerService;

    public CompositionConsumerInternalService(CompositionConfig config, ConsumerInternalService igniteConsumerService,
                                              ConsumerInternalService journalkeeperConsumerService) {
        this.config = config;
        this.igniteConsumerService = igniteConsumerService;
        this.journalkeeperConsumerService = journalkeeperConsumerService;
    }

    @Override
    public Consumer getByTopicAndApp(TopicName topic, String app) {
        if (config.isReadIgnite()) {
            return igniteConsumerService.getByTopicAndApp(topic, app);
        } else {
            return journalkeeperConsumerService.getByTopicAndApp(topic, app);
        }
    }

    @Override
    public List<Consumer> getByTopic(TopicName topic) {
        if (config.isReadIgnite()) {
            return igniteConsumerService.getByTopic(topic);
        } else {
            return journalkeeperConsumerService.getByTopic(topic);
        }
    }

    @Override
    public List<Consumer> getByApp(String app) {
        if (config.isReadIgnite()) {
            return igniteConsumerService.getByApp(app);
        } else {
            return journalkeeperConsumerService.getByApp(app);
        }
    }

    @Override
    public List<Consumer> getAll() {
        if (config.isReadIgnite()) {
            return igniteConsumerService.getAll();
        } else {
            return journalkeeperConsumerService.getAll();
        }
    }

    @Override
    public Consumer add(Consumer consumer) {
        Consumer result = null;
        if (config.isWriteIgnite()) {
            result = igniteConsumerService.add(consumer);
        }
        if (config.isWriteJournalkeeper()) {
            try {
                journalkeeperConsumerService.add(consumer);
            } catch (Exception e) {
                logger.error("add journalkeeper exception, params: {}", consumer, e);
            }
        }
        return result;
    }

    @Override
    public Consumer update(Consumer consumer) {
        Consumer result = null;
        if (config.isWriteIgnite()) {
            result = igniteConsumerService.update(consumer);
        }
        if (config.isWriteJournalkeeper()) {
            try {
                journalkeeperConsumerService.update(consumer);
            } catch (Exception e) {
                logger.error("update journalkeeper exception, params: {}", consumer, e);
            }
        }
        return result;
    }

    @Override
    public void delete(String id) {
        if (config.isWriteIgnite()) {
            igniteConsumerService.delete(id);
        }
        if (config.isWriteJournalkeeper()) {
            try {
                journalkeeperConsumerService.delete(id);
            } catch (Exception e) {
                logger.error("delete journalkeeper exception, params: {}", id, e);
            }
        }
    }

    @Override
    public Consumer getById(String id) {
        if (config.isReadIgnite()) {
            return igniteConsumerService.getById(id);
        } else {
            return journalkeeperConsumerService.getById(id);
        }
    }
}
