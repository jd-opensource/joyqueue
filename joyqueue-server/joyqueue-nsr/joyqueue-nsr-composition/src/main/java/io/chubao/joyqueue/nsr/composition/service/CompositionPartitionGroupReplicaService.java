package io.chubao.joyqueue.nsr.composition.service;

import io.chubao.joyqueue.domain.Replica;
import io.chubao.joyqueue.domain.TopicName;
import io.chubao.joyqueue.model.PageResult;
import io.chubao.joyqueue.model.QPageQuery;
import io.chubao.joyqueue.nsr.composition.config.CompositionConfig;
import io.chubao.joyqueue.nsr.model.ReplicaQuery;
import io.chubao.joyqueue.nsr.service.PartitionGroupReplicaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * CompositionPartitionGroupReplicaService
 * author: gaohaoxiang
 * date: 2019/8/12
 */
public class CompositionPartitionGroupReplicaService implements PartitionGroupReplicaService {

    protected static final Logger logger = LoggerFactory.getLogger(CompositionPartitionGroupReplicaService.class);

    private CompositionConfig config;
    private PartitionGroupReplicaService ignitePartitionGroupReplicaService;
    private PartitionGroupReplicaService journalkeeperPartitionGroupReplicaService;

    public CompositionPartitionGroupReplicaService(CompositionConfig config, PartitionGroupReplicaService ignitePartitionGroupReplicaService,
                                                   PartitionGroupReplicaService journalkeeperPartitionGroupReplicaService) {
        this.config = config;
        this.ignitePartitionGroupReplicaService = ignitePartitionGroupReplicaService;
        this.journalkeeperPartitionGroupReplicaService = journalkeeperPartitionGroupReplicaService;
    }

    @Override
    public void deleteByTopic(TopicName topic) {
        if (config.isWriteIgnite()) {
            ignitePartitionGroupReplicaService.deleteByTopic(topic);
        }
        if (config.isWriteJournalkeeper()) {
            try {
                journalkeeperPartitionGroupReplicaService.deleteByTopic(topic);
            } catch (Exception e) {
                logger.error("deleteByTopic journalkeeper exception, params: {}", topic, e);
            }
        }
    }

    @Override
    public void deleteByTopicAndPartitionGroup(TopicName topic, int groupNo) {
        if (config.isWriteIgnite()) {
            ignitePartitionGroupReplicaService.deleteByTopicAndPartitionGroup(topic, groupNo);
        }
        if (config.isWriteJournalkeeper()) {
            try {
                journalkeeperPartitionGroupReplicaService.deleteByTopicAndPartitionGroup(topic, groupNo);
            } catch (Exception e) {
                logger.error("deleteByTopicAndPartitionGroup journalkeeper exception, params: {}, {}", topic, groupNo, e);
            }
        }
    }

    @Override
    public List<Replica> findByTopic(TopicName topic) {
        if (config.isReadIgnite()) {
            return ignitePartitionGroupReplicaService.findByTopic(topic);
        } else {
            return journalkeeperPartitionGroupReplicaService.findByTopic(topic);
        }
    }

    @Override
    public List<Replica> findByTopicAndGrPartitionGroup(TopicName topic, int groupNo) {
        if (config.isReadIgnite()) {
            return ignitePartitionGroupReplicaService.findByTopicAndGrPartitionGroup(topic, groupNo);
        } else {
            return journalkeeperPartitionGroupReplicaService.findByTopicAndGrPartitionGroup(topic, groupNo);
        }
    }

    @Override
    public List<Replica> findByBrokerId(Integer brokerId) {
        if (config.isReadIgnite()) {
            return ignitePartitionGroupReplicaService.findByBrokerId(brokerId);
        } else {
            return journalkeeperPartitionGroupReplicaService.findByBrokerId(brokerId);
        }
    }

    @Override
    public Replica getById(String id) {
        if (config.isReadIgnite()) {
            return ignitePartitionGroupReplicaService.getById(id);
        } else {
            return journalkeeperPartitionGroupReplicaService.getById(id);
        }
    }

    @Override
    public Replica get(Replica model) {
        if (config.isReadIgnite()) {
            return ignitePartitionGroupReplicaService.get(model);
        } else {
            return journalkeeperPartitionGroupReplicaService.get(model);
        }
    }

    @Override
    public void addOrUpdate(Replica replica) {
        if (config.isWriteIgnite()) {
            ignitePartitionGroupReplicaService.addOrUpdate(replica);
        }
        if (config.isWriteJournalkeeper()) {
            try {
                journalkeeperPartitionGroupReplicaService.addOrUpdate(replica);
            } catch (Exception e) {
                logger.error("addOrUpdate journalkeeper exception, params: {}", replica, e);
            }
        }
    }

    @Override
    public void deleteById(String id) {
        if (config.isWriteIgnite()) {
            ignitePartitionGroupReplicaService.deleteById(id);
        }
        if (config.isWriteJournalkeeper()) {
            try {
                journalkeeperPartitionGroupReplicaService.deleteById(id);
            } catch (Exception e) {
                logger.error("deleteById journalkeeper exception, params: {}", id, e);
            }
        }
    }

    @Override
    public void delete(Replica model) {
        if (config.isWriteIgnite()) {
            ignitePartitionGroupReplicaService.delete(model);
        }
        if (config.isWriteJournalkeeper()) {
            try {
                journalkeeperPartitionGroupReplicaService.delete(model);
            } catch (Exception e) {
                logger.error("delete journalkeeper exception, params: {}", model, e);
            }
        }
    }

    @Override
    public List<Replica> list() {
        if (config.isReadIgnite()) {
            return ignitePartitionGroupReplicaService.list();
        } else {
            return journalkeeperPartitionGroupReplicaService.list();
        }
    }

    @Override
    public List<Replica> list(ReplicaQuery query) {
        if (config.isReadIgnite()) {
            return ignitePartitionGroupReplicaService.list(query);
        } else {
            return journalkeeperPartitionGroupReplicaService.list(query);
        }
    }

    @Override
    public PageResult<Replica> pageQuery(QPageQuery<ReplicaQuery> pageQuery) {
        if (config.isReadIgnite()) {
            return ignitePartitionGroupReplicaService.pageQuery(pageQuery);
        } else {
            return journalkeeperPartitionGroupReplicaService.pageQuery(pageQuery);
        }
    }
}
