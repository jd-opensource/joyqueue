package io.chubao.joyqueue.nsr.composition.service;

import io.chubao.joyqueue.domain.Consumer;
import io.chubao.joyqueue.domain.TopicName;
import io.chubao.joyqueue.model.PageResult;
import io.chubao.joyqueue.model.QPageQuery;
import io.chubao.joyqueue.nsr.composition.config.CompositionConfig;
import io.chubao.joyqueue.nsr.model.ConsumerQuery;
import io.chubao.joyqueue.nsr.service.ConsumerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * CompositionConsumerService
 * author: gaohaoxiang
 * date: 2019/8/12
 */
public class CompositionConsumerService implements ConsumerService {

    protected static final Logger logger = LoggerFactory.getLogger(CompositionConsumerService.class);

    private CompositionConfig config;
    private ConsumerService igniteConsumerService;
    private ConsumerService journalkeeperConsumerService;

    public CompositionConsumerService(CompositionConfig config, ConsumerService igniteConsumerService,
                                      ConsumerService journalkeeperConsumerService) {
        this.config = config;
        this.igniteConsumerService = igniteConsumerService;
        this.journalkeeperConsumerService = journalkeeperConsumerService;
    }

    @Override
    public void deleteByTopicAndApp(TopicName topic, String app) {
        if (config.isWriteIgnite()) {
            igniteConsumerService.deleteByTopicAndApp(topic, app);
        }
        if (config.isWriteJournalkeeper()) {
            try {
                journalkeeperConsumerService.deleteByTopicAndApp(topic, app);
            } catch (Exception e) {
                logger.error("deleteByTopicAndApp journalkeeper exception, params: {}, {}", topic, app, e);
            }
        }
    }

    @Override
    public Consumer getByTopicAndApp(TopicName topic, String app) {
        return null;
    }

    @Override
    public List<Consumer> getByTopic(TopicName topic, boolean withConfig) {
        return null;
    }

    @Override
    public List<Consumer> getByApp(String app, boolean withConfig) {
        return null;
    }

    @Override
    public List<Consumer> getConsumerByClientType(byte clientType) {
        return null;
    }

    @Override
    public void add(Consumer consumer) {
        if (config.isWriteIgnite()) {
            igniteConsumerService.add(consumer);
        }
        if (config.isWriteJournalkeeper()) {
            try {
                journalkeeperConsumerService.add(consumer);
            } catch (Exception e) {
                logger.error("add journalkeeper exception, params: {}", consumer, e);
            }
        }
    }

    @Override
    public void update(Consumer consumer) {
        if (config.isWriteIgnite()) {
            igniteConsumerService.update(consumer);
        }
        if (config.isWriteJournalkeeper()) {
            try {
                journalkeeperConsumerService.update(consumer);
            } catch (Exception e) {
                logger.error("update journalkeeper exception, params: {}", consumer, e);
            }
        }
    }

    @Override
    public void remove(Consumer consumer) {
        if (config.isWriteIgnite()) {
            igniteConsumerService.remove(consumer);
        }
        if (config.isWriteJournalkeeper()) {
            try {
                journalkeeperConsumerService.remove(consumer);
            } catch (Exception e) {
                logger.error("remove journalkeeper exception, params: {}", consumer, e);
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

    @Override
    public Consumer get(Consumer model) {
        if (config.isReadIgnite()) {
            return igniteConsumerService.get(model);
        } else {
            return journalkeeperConsumerService.get(model);
        }
    }

    @Override
    public void addOrUpdate(Consumer consumer) {
        if (config.isWriteIgnite()) {
            igniteConsumerService.addOrUpdate(consumer);
        }
        if (config.isWriteJournalkeeper()) {
            try {
                journalkeeperConsumerService.addOrUpdate(consumer);
            } catch (Exception e) {
                logger.error("addOrUpdate journalkeeper exception, params: {}", consumer, e);
            }
        }
    }

    @Override
    public void deleteById(String id) {
        if (config.isWriteIgnite()) {
            igniteConsumerService.deleteById(id);
        }
        if (config.isWriteJournalkeeper()) {
            try {
                journalkeeperConsumerService.deleteById(id);
            } catch (Exception e) {
                logger.error("deleteById journalkeeper exception, params: {}", id, e);
            }
        }
    }

    @Override
    public void delete(Consumer model) {
        if (config.isWriteIgnite()) {
            igniteConsumerService.delete(model);
        }
        if (config.isWriteJournalkeeper()) {
            try {
                journalkeeperConsumerService.delete(model);
            } catch (Exception e) {
                logger.error("delete journalkeeper exception, params: {}", model, e);
            }
        }
    }

    @Override
    public List<Consumer> list() {
        if (config.isReadIgnite()) {
            return igniteConsumerService.list();
        } else {
            return journalkeeperConsumerService.list();
        }
    }

    @Override
    public List<Consumer> list(ConsumerQuery query) {
        if (config.isReadIgnite()) {
            return igniteConsumerService.list(query);
        } else {
            return journalkeeperConsumerService.list(query);
        }
    }

    @Override
    public PageResult<Consumer> pageQuery(QPageQuery<ConsumerQuery> pageQuery) {
        if (config.isReadIgnite()) {
            return igniteConsumerService.pageQuery(pageQuery);
        } else {
            return journalkeeperConsumerService.pageQuery(pageQuery);
        }
    }
}
