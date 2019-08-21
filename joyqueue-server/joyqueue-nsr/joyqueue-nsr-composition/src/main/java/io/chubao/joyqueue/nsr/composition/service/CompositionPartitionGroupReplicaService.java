package io.chubao.joyqueue.nsr.composition.service;

import io.chubao.joyqueue.domain.Replica;
import io.chubao.joyqueue.domain.TopicName;
import io.chubao.joyqueue.nsr.composition.config.CompositionConfig;
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
    public List<Replica> getByTopic(TopicName topic) {
        if (config.isReadIgnite()) {
            return ignitePartitionGroupReplicaService.getByTopic(topic);
        } else {
            return journalkeeperPartitionGroupReplicaService.getByTopic(topic);
        }
    }

    @Override
    public List<Replica> getByTopicAndGroup(TopicName topic, int groupNo) {
        if (config.isReadIgnite()) {
            return ignitePartitionGroupReplicaService.getByTopicAndGroup(topic, groupNo);
        } else {
            return journalkeeperPartitionGroupReplicaService.getByTopicAndGroup(topic, groupNo);
        }
    }

    @Override
    public List<Replica> getByBrokerId(Integer brokerId) {
        if (config.isReadIgnite()) {
            return ignitePartitionGroupReplicaService.getByBrokerId(brokerId);
        } else {
            return journalkeeperPartitionGroupReplicaService.getByBrokerId(brokerId);
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
    public Replica add(Replica replica) {
        Replica result = null;
        if (config.isWriteIgnite()) {
            result = ignitePartitionGroupReplicaService.add(replica);
        }
        if (config.isWriteJournalkeeper()) {
            try {
                journalkeeperPartitionGroupReplicaService.add(replica);
            } catch (Exception e) {
                logger.error("add journalkeeper exception, params: {}", replica, e);
            }
        }
        return result;
    }

    @Override
    public Replica update(Replica replica) {
        Replica result = null;
        if (config.isWriteIgnite()) {
            result = ignitePartitionGroupReplicaService.update(replica);
        }
        if (config.isWriteJournalkeeper()) {
            try {
                journalkeeperPartitionGroupReplicaService.update(replica);
            } catch (Exception e) {
                logger.error("update journalkeeper exception, params: {}", replica, e);
            }
        }
        return result;
    }

    @Override
    public void delete(String id) {
        if (config.isWriteIgnite()) {
            ignitePartitionGroupReplicaService.delete(id);
        }
        if (config.isWriteJournalkeeper()) {
            try {
                journalkeeperPartitionGroupReplicaService.delete(id);
            } catch (Exception e) {
                logger.error("delete journalkeeper exception, params: {}", id, e);
            }
        }
    }
}
