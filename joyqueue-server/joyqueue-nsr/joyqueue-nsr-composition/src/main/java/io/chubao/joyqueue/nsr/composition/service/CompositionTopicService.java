package io.chubao.joyqueue.nsr.composition.service;

import io.chubao.joyqueue.domain.PartitionGroup;
import io.chubao.joyqueue.domain.Topic;
import io.chubao.joyqueue.model.PageResult;
import io.chubao.joyqueue.model.QPageQuery;
import io.chubao.joyqueue.nsr.composition.config.CompositionConfig;
import io.chubao.joyqueue.nsr.model.TopicQuery;
import io.chubao.joyqueue.nsr.service.TopicService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;

/**
 * CompositionTopicService
 * author: gaohaoxiang
 * date: 2019/8/12
 */
public class CompositionTopicService implements TopicService {

    protected static final Logger logger = LoggerFactory.getLogger(CompositionTopicService.class);

    private CompositionConfig config;
    private TopicService igniteTopicService;
    private TopicService journalkeeperTopicService;

    public CompositionTopicService(CompositionConfig config, TopicService igniteTopicService, TopicService journalkeeperTopicService) {
        this.config = config;
        this.igniteTopicService = igniteTopicService;
        this.journalkeeperTopicService = journalkeeperTopicService;
    }

    @Override
    public Topic getTopicByCode(String namespace, String topic) {
        if (config.isReadIgnite()) {
            return igniteTopicService.getTopicByCode(namespace, topic);
        } else {
            return journalkeeperTopicService.getTopicByCode(namespace, topic);
        }
    }

    @Override
    public PageResult<Topic> findUnsubscribedByQuery(QPageQuery<TopicQuery> pageQuery) {
        if (config.isReadIgnite()) {
            return igniteTopicService.findUnsubscribedByQuery(pageQuery);
        } else {
            return journalkeeperTopicService.findUnsubscribedByQuery(pageQuery);
        }
    }

    @Override
    public void addTopic(Topic topic, List<PartitionGroup> partitionGroups) {
        if (config.isWriteIgnite()) {
            igniteTopicService.addTopic(topic, partitionGroups);
        }
        if (config.isWriteJournalkeeper()) {
            try {
                journalkeeperTopicService.addTopic(topic, partitionGroups);
            } catch (Exception e) {
                logger.error("add journalkeeper exception, params: {}, {}", topic, partitionGroups, e);
            }
        }
    }

    @Override
    public void removeTopic(Topic topic) {
        if (config.isWriteIgnite()) {
            igniteTopicService.removeTopic(topic);
        }
        if (config.isWriteJournalkeeper()) {
            try {
                journalkeeperTopicService.removeTopic(topic);
            } catch (Exception e) {
                logger.error("removeTopic journalkeeper exception, params: {}", topic, e);
            }
        }
    }

    @Override
    public void addPartitionGroup(PartitionGroup group) {
        if (config.isWriteIgnite()) {
            igniteTopicService.addPartitionGroup(group);
        }
        if (config.isWriteJournalkeeper()) {
            try {
                journalkeeperTopicService.addPartitionGroup(group);
            } catch (Exception e) {
                logger.error("addPartitionGroup journalkeeper exception, params: {}", group, e);
            }
        }
    }

    @Override
    public void removePartitionGroup(PartitionGroup group) {
        if (config.isWriteIgnite()) {
            igniteTopicService.removePartitionGroup(group);
        }
        if (config.isWriteJournalkeeper()) {
            try {
                journalkeeperTopicService.removePartitionGroup(group);
            } catch (Exception e) {
                logger.error("removePartitionGroup journalkeeper exception, params: {}", group, e);
            }
        }
    }

    @Override
    public Collection<Integer> updatePartitionGroup(PartitionGroup group) {
        Collection<Integer> result = null;
        if (config.isWriteIgnite()) {
            result = igniteTopicService.updatePartitionGroup(group);
        }
        if (config.isWriteJournalkeeper()) {
            try {
                result = journalkeeperTopicService.updatePartitionGroup(group);
            } catch (Exception e) {
                logger.error("updatePartitionGroup journalkeeper exception, params: {}", group, e);
            }
        }
        return result;
    }

    @Override
    public void leaderChange(PartitionGroup group) {
        if (config.isWriteIgnite()) {
            igniteTopicService.leaderChange(group);
        }
        if (config.isWriteJournalkeeper()) {
            try {
                journalkeeperTopicService.leaderChange(group);
            } catch (Exception e) {
                logger.error("leaderChange journalkeeper exception, params: {}", group, e);
            }
        }
    }

    @Override
    public List<PartitionGroup> getPartitionGroup(String namesapce, String topic, Object[] groups) {
        if (config.isReadIgnite()) {
            return igniteTopicService.getPartitionGroup(namesapce, topic, groups);
        } else {
            return journalkeeperTopicService.getPartitionGroup(namesapce, topic, groups);
        }
    }

    @Override
    public Topic getById(String id) {
        if (config.isReadIgnite()) {
            return igniteTopicService.getById(id);
        } else {
            return journalkeeperTopicService.getById(id);
        }
    }

    @Override
    public Topic get(Topic model) {
        if (config.isReadIgnite()) {
            return igniteTopicService.get(model);
        } else {
            return journalkeeperTopicService.get(model);
        }
    }

    @Override
    public void addOrUpdate(Topic topic) {
        if (config.isWriteIgnite()) {
            igniteTopicService.addOrUpdate(topic);
        }
        if (config.isWriteJournalkeeper()) {
            try {
                journalkeeperTopicService.addOrUpdate(topic);
            } catch (Exception e) {
                logger.error("addOrUpdate journalkeeper exception, params: {}", topic, e);
            }
        }
    }

    @Override
    public void deleteById(String id) {
        if (config.isWriteIgnite()) {
            igniteTopicService.deleteById(id);
        }
        if (config.isWriteJournalkeeper()) {
            try {
                journalkeeperTopicService.deleteById(id);
            } catch (Exception e) {
                logger.error("deleteById journalkeeper exception, params: {}", id, e);
            }
        }
    }

    @Override
    public void delete(Topic model) {
        if (config.isWriteIgnite()) {
            igniteTopicService.delete(model);
        }
        if (config.isWriteJournalkeeper()) {
            try {
                journalkeeperTopicService.delete(model);
            } catch (Exception e) {
                logger.error("delete journalkeeper exception, params: {}", model, e);
            }
        }
    }

    @Override
    public List<Topic> list() {
        if (config.isReadIgnite()) {
            return igniteTopicService.list();
        } else {
            return journalkeeperTopicService.list();
        }
    }

    @Override
    public List<Topic> list(TopicQuery query) {
        if (config.isReadIgnite()) {
            return igniteTopicService.list(query);
        } else {
            return journalkeeperTopicService.list(query);
        }
    }

    @Override
    public PageResult<Topic> pageQuery(QPageQuery<TopicQuery> pageQuery) {
        if (config.isReadIgnite()) {
            return igniteTopicService.pageQuery(pageQuery);
        } else {
            return journalkeeperTopicService.pageQuery(pageQuery);
        }
    }
}