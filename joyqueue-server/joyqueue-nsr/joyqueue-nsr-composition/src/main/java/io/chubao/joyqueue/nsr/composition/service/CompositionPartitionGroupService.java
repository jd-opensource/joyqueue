package io.chubao.joyqueue.nsr.composition.service;

import io.chubao.joyqueue.domain.PartitionGroup;
import io.chubao.joyqueue.domain.TopicName;
import io.chubao.joyqueue.model.PageResult;
import io.chubao.joyqueue.model.QPageQuery;
import io.chubao.joyqueue.nsr.composition.config.CompositionConfig;
import io.chubao.joyqueue.nsr.model.PartitionGroupQuery;
import io.chubao.joyqueue.nsr.service.PartitionGroupService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * CompositionPartitionGroupService
 * author: gaohaoxiang
 * date: 2019/8/12
 */
public class CompositionPartitionGroupService implements PartitionGroupService {

    protected final Logger logger = LoggerFactory.getLogger(CompositionPartitionGroupService.class);

    private CompositionConfig config;
    private PartitionGroupService ignitePartitionGroupService;
    private PartitionGroupService journalkeeperPartitionGroupService;

    public CompositionPartitionGroupService(CompositionConfig config, PartitionGroupService ignitePartitionGroupService,
                                            PartitionGroupService journalkeeperPartitionGroupService) {
        this.config = config;
        this.ignitePartitionGroupService = ignitePartitionGroupService;
        this.journalkeeperPartitionGroupService = journalkeeperPartitionGroupService;
    }

    @Override
    public PartitionGroup findByTopicAndGroup(TopicName topic, int group) {
        if (config.isReadIgnite()) {
            return ignitePartitionGroupService.findByTopicAndGroup(topic, group);
        } else {
            return journalkeeperPartitionGroupService.findByTopicAndGroup(topic, group);
        }
    }

    @Override
    public List<PartitionGroup> getByTopic(TopicName topic) {
        if (config.isReadIgnite()) {
            return ignitePartitionGroupService.getByTopic(topic);
        } else {
            return journalkeeperPartitionGroupService.getByTopic(topic);
        }
    }

    @Override
    public PartitionGroup getById(String id) {
        if (config.isReadIgnite()) {
            return ignitePartitionGroupService.getById(id);
        } else {
            return journalkeeperPartitionGroupService.getById(id);
        }
    }

    @Override
    public PartitionGroup get(PartitionGroup model) {
        if (config.isReadIgnite()) {
            return ignitePartitionGroupService.get(model);
        } else {
            return journalkeeperPartitionGroupService.get(model);
        }
    }

    @Override
    public void addOrUpdate(PartitionGroup partitionGroup) {
        if (config.isWriteIgnite()) {
            ignitePartitionGroupService.addOrUpdate(partitionGroup);
        }
        if (config.isWriteJournalkeeper()) {
            try {
                journalkeeperPartitionGroupService.addOrUpdate(partitionGroup);
            } catch (Exception e) {
                logger.error("addOrUpdate journalkeeper exception, params: {}", partitionGroup, e);
            }
        }
    }

    @Override
    public void deleteById(String id) {
        if (config.isWriteIgnite()) {
            ignitePartitionGroupService.deleteById(id);
        }
        if (config.isWriteJournalkeeper()) {
            try {
                journalkeeperPartitionGroupService.deleteById(id);
            } catch (Exception e) {
                logger.error("deleteById journalkeeper exception, params: {}", id, e);
            }
        }
    }

    @Override
    public void delete(PartitionGroup model) {
        if (config.isWriteIgnite()) {
            ignitePartitionGroupService.delete(model);
        }
        if (config.isWriteJournalkeeper()) {
            try {
                journalkeeperPartitionGroupService.delete(model);
            } catch (Exception e) {
                logger.error("delete journalkeeper exception, params: {}", model, e);
            }
        }
    }

    @Override
    public List<PartitionGroup> list() {
        if (config.isReadIgnite()) {
            return ignitePartitionGroupService.list();
        } else {
            return journalkeeperPartitionGroupService.list();
        }
    }

    @Override
    public List<PartitionGroup> list(PartitionGroupQuery query) {
        if (config.isReadIgnite()) {
            return ignitePartitionGroupService.list(query);
        } else {
            return journalkeeperPartitionGroupService.list(query);
        }
    }

    @Override
    public PageResult<PartitionGroup> pageQuery(QPageQuery<PartitionGroupQuery> pageQuery) {
        if (config.isReadIgnite()) {
            return ignitePartitionGroupService.pageQuery(pageQuery);
        } else {
            return journalkeeperPartitionGroupService.pageQuery(pageQuery);
        }
    }
}
