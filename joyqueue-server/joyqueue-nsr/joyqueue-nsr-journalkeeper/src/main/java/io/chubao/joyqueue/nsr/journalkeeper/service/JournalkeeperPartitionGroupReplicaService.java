package io.chubao.joyqueue.nsr.journalkeeper.service;

import io.chubao.joyqueue.domain.Replica;
import io.chubao.joyqueue.domain.TopicName;
import io.chubao.joyqueue.model.PageResult;
import io.chubao.joyqueue.model.QPageQuery;
import io.chubao.joyqueue.nsr.journalkeeper.converter.PartitionGroupReplicaConverter;
import io.chubao.joyqueue.nsr.journalkeeper.repository.PartitionGroupReplicaRepository;
import io.chubao.joyqueue.nsr.model.ReplicaQuery;
import io.chubao.joyqueue.nsr.service.PartitionGroupReplicaService;

import java.util.List;

/**
 * JournalkeeperPartitionGroupReplicaService
 * author: gaohaoxiang
 * date: 2019/8/16
 */
public class JournalkeeperPartitionGroupReplicaService implements PartitionGroupReplicaService {

    private PartitionGroupReplicaRepository partitionGroupReplicaRepository;

    public JournalkeeperPartitionGroupReplicaService(PartitionGroupReplicaRepository partitionGroupReplicaRepository) {
        this.partitionGroupReplicaRepository = partitionGroupReplicaRepository;
    }

    @Override
    public void deleteByTopic(TopicName topic) {

    }

    @Override
    public void deleteByTopicAndPartitionGroup(TopicName topic, int groupNo) {

    }

    @Override
    public List<Replica> findByTopic(TopicName topic) {
        return PartitionGroupReplicaConverter.convert(partitionGroupReplicaRepository.getByTopic(topic.getCode(), topic.getNamespace()));
    }

    @Override
    public List<Replica> findByTopicAndGrPartitionGroup(TopicName topic, int groupNo) {
        return PartitionGroupReplicaConverter.convert(partitionGroupReplicaRepository.getByTopicAndGroup(topic.getCode(), topic.getNamespace(), groupNo));
    }

    @Override
    public List<Replica> findByBrokerId(Integer brokerId) {
        return PartitionGroupReplicaConverter.convert(partitionGroupReplicaRepository.getByBrokerId(brokerId));
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
        partitionGroupReplicaRepository.add(PartitionGroupReplicaConverter.convert(replica));
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