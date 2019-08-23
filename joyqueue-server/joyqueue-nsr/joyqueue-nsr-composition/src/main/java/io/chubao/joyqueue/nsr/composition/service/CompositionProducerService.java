package io.chubao.joyqueue.nsr.composition.service;

import io.chubao.joyqueue.domain.Producer;
import io.chubao.joyqueue.domain.TopicName;
import io.chubao.joyqueue.nsr.composition.config.CompositionConfig;
import io.chubao.joyqueue.nsr.service.ProducerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * CompositionProducerService
 * author: gaohaoxiang
 * date: 2019/8/12
 */
public class CompositionProducerService implements ProducerService {

    protected static final Logger logger = LoggerFactory.getLogger(CompositionProducerService.class);

    private CompositionConfig config;
    private ProducerService igniteProducerService;
    private ProducerService journalkeeperProducerService;

    public CompositionProducerService(CompositionConfig config, ProducerService igniteProducerService,
                                      ProducerService journalkeeperProducerService) {
        this.config = config;
        this.igniteProducerService = igniteProducerService;
        this.journalkeeperProducerService = journalkeeperProducerService;
    }

    @Override
    public Producer getById(String id) {
        if (config.isReadIgnite()) {
            return igniteProducerService.getById(id);
        } else {
            return journalkeeperProducerService.getById(id);
        }
    }

    @Override
    public Producer getByTopicAndApp(TopicName topic, String app) {
        if (config.isReadIgnite()) {
            return igniteProducerService.getByTopicAndApp(topic, app);
        } else {
            return journalkeeperProducerService.getByTopicAndApp(topic, app);
        }
    }

    @Override
    public List<Producer> getByTopic(TopicName topic) {
        if (config.isReadIgnite()) {
            return igniteProducerService.getByTopic(topic);
        } else {
            return journalkeeperProducerService.getByTopic(topic);
        }
    }

    @Override
    public List<Producer> getByApp(String app) {
        if (config.isReadIgnite()) {
            return igniteProducerService.getByApp(app);
        } else {
            return journalkeeperProducerService.getByApp(app);
        }
    }

    @Override
    public Producer add(Producer producer) {
        Producer result = null;
        if (config.isWriteIgnite()) {
            result = igniteProducerService.add(producer);
        }
        if (config.isWriteJournalkeeper()) {
            try {
                journalkeeperProducerService.add(producer);
            } catch (Exception e) {
                logger.error("add journalkeeper exception, params: {}", producer, e);
            }
        }
        return result;
    }

    @Override
    public Producer update(Producer producer) {
        Producer result = null;
        if (config.isWriteIgnite()) {
            result = igniteProducerService.update(producer);
        }
        if (config.isWriteJournalkeeper()) {
            try {
                journalkeeperProducerService.update(producer);
            } catch (Exception e) {
                logger.error("update journalkeeper exception, params: {}", producer, e);
            }
        }
        return result;
    }

    @Override
    public void delete(String id) {
        if (config.isWriteIgnite()) {
            igniteProducerService.delete(id);
        }
        if (config.isWriteJournalkeeper()) {
            try {
                journalkeeperProducerService.delete(id);
            } catch (Exception e) {
                logger.error("delete journalkeeper exception, params: {}", id, e);
            }
        }
    }
}
