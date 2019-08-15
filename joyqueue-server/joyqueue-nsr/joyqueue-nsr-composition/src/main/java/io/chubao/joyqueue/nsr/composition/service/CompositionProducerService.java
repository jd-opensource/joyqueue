package io.chubao.joyqueue.nsr.composition.service;

import io.chubao.joyqueue.domain.Producer;
import io.chubao.joyqueue.domain.TopicName;
import io.chubao.joyqueue.model.PageResult;
import io.chubao.joyqueue.model.QPageQuery;
import io.chubao.joyqueue.nsr.composition.config.CompositionConfig;
import io.chubao.joyqueue.nsr.model.ProducerQuery;
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
    public void deleteByTopicAndApp(TopicName topic, String app) {
        if (config.isWriteIgnite()) {
            igniteProducerService.deleteByTopicAndApp(topic, app);
        }
        if (config.isWriteJournalkeeper()) {
            try {
                journalkeeperProducerService.deleteByTopicAndApp(topic, app);
            } catch (Exception e) {
                logger.error("deleteByTopicAndApp journalkeeper exception, params: {}, {}", topic, app, e);
            }
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
    public List<Producer> getByTopic(TopicName topic, boolean withConfig) {
        if (config.isReadIgnite()) {
            return igniteProducerService.getByTopic(topic, withConfig);
        } else {
            return journalkeeperProducerService.getByTopic(topic, withConfig);
        }
    }

    @Override
    public List<Producer> getByApp(String app, boolean withConfig) {
        if (config.isReadIgnite()) {
            return igniteProducerService.getByApp(app, withConfig);
        } else {
            return journalkeeperProducerService.getByApp(app, withConfig);
        }
    }

    @Override
    public void add(Producer producer) {
        if (config.isWriteIgnite()) {
            igniteProducerService.add(producer);
        }
        if (config.isWriteJournalkeeper()) {
            try {
                journalkeeperProducerService.add(producer);
            } catch (Exception e) {
                logger.error("add journalkeeper exception, params: {}", producer, e);
            }
        }
    }

    @Override
    public void update(Producer producer) {
        if (config.isWriteIgnite()) {
            igniteProducerService.update(producer);
        }
        if (config.isWriteJournalkeeper()) {
            try {
                journalkeeperProducerService.update(producer);
            } catch (Exception e) {
                logger.error("update journalkeeper exception, params: {}", producer, e);
            }
        }
    }

    @Override
    public void remove(Producer producer) {
        if (config.isWriteIgnite()) {
            igniteProducerService.remove(producer);
        }
        if (config.isWriteJournalkeeper()) {
            try {
                journalkeeperProducerService.remove(producer);
            } catch (Exception e) {
                logger.error("remove journalkeeper exception, params: {}", producer, e);
            }
        }
    }

    @Override
    public List<Producer> getProducerByClientType(byte clientType) {
        if (config.isReadIgnite()) {
            return igniteProducerService.getProducerByClientType(clientType);
        } else {
            return journalkeeperProducerService.getProducerByClientType(clientType);
        }
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
    public Producer get(Producer model) {
        if (config.isReadIgnite()) {
            return igniteProducerService.get(model);
        } else {
            return journalkeeperProducerService.get(model);
        }
    }

    @Override
    public void addOrUpdate(Producer producer) {
        if (config.isWriteIgnite()) {
            igniteProducerService.addOrUpdate(producer);
        }
        if (config.isWriteJournalkeeper()) {
            try {
                journalkeeperProducerService.addOrUpdate(producer);
            } catch (Exception e) {
                logger.error("addOrUpdate journalkeeper exception, params: {}", producer, e);
            }
        }
    }

    @Override
    public void deleteById(String id) {
        if (config.isWriteIgnite()) {
            igniteProducerService.deleteById(id);
        }
        if (config.isWriteJournalkeeper()) {
            try {
                journalkeeperProducerService.deleteById(id);
            } catch (Exception e) {
                logger.error("deleteById journalkeeper exception, params: {}", id, e);
            }
        }
    }

    @Override
    public void delete(Producer model) {
        if (config.isWriteIgnite()) {
            igniteProducerService.delete(model);
        }
        if (config.isWriteJournalkeeper()) {
            try {
                journalkeeperProducerService.delete(model);
            } catch (Exception e) {
                logger.error("delete journalkeeper exception, params: {}", model, e);
            }
        }
    }

    @Override
    public List<Producer> list() {
        if (config.isReadIgnite()) {
            return igniteProducerService.list();
        } else {
            return journalkeeperProducerService.list();
        }
    }

    @Override
    public List<Producer> list(ProducerQuery query) {
        if (config.isReadIgnite()) {
            return igniteProducerService.list(query);
        } else {
            return journalkeeperProducerService.list(query);
        }
    }

    @Override
    public PageResult<Producer> pageQuery(QPageQuery<ProducerQuery> pageQuery) {
        if (config.isReadIgnite()) {
            return igniteProducerService.pageQuery(pageQuery);
        } else {
            return journalkeeperProducerService.pageQuery(pageQuery);
        }
    }
}
