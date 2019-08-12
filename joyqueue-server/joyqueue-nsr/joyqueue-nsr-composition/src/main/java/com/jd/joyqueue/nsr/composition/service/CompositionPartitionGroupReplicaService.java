package com.jd.joyqueue.nsr.composition.service;

import com.jd.joyqueue.nsr.composition.config.CompositionConfig;
import com.jd.joyqueue.domain.Replica;
import com.jd.joyqueue.domain.TopicName;
import com.jd.joyqueue.model.PageResult;
import com.jd.joyqueue.model.QPageQuery;
import com.jd.joyqueue.nsr.model.ReplicaQuery;
import com.jd.joyqueue.nsr.service.PartitionGroupReplicaService;

import java.util.List;

/**
 * CompositionPartitionGroupReplicaService
 * author: gaohaoxiang
 * date: 2019/8/12
 */
public class CompositionPartitionGroupReplicaService implements PartitionGroupReplicaService {

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

    }

    @Override
    public void deleteByTopicAndPartitionGroup(TopicName topic, int groupNo) {

    }

    @Override
    public List<Replica> findByTopic(TopicName topic) {
        return null;
    }

    @Override
    public List<Replica> findByTopicAndGrPartitionGroup(TopicName topic, int groupNo) {
        return null;
    }

    @Override
    public List<Replica> findByBrokerId(Integer brokerId) {
        return null;
    }

    @Override
    public Replica getById(String id) {
        return null;
    }

    @Override
    public Replica get(Replica model) {
        return null;
    }

    @Override
    public void addOrUpdate(Replica replica) {

    }

    @Override
    public void deleteById(String id) {

    }

    @Override
    public void delete(Replica model) {

    }

    @Override
    public List<Replica> list() {
        return null;
    }

    @Override
    public List<Replica> list(ReplicaQuery query) {
        return null;
    }

    @Override
    public PageResult<Replica> pageQuery(QPageQuery<ReplicaQuery> pageQuery) {
        return null;
    }
}
