package io.chubao.joyqueue.nsr.journalkeeper.service;

import io.chubao.joyqueue.domain.Replica;
import io.chubao.joyqueue.domain.TopicName;
import io.chubao.joyqueue.nsr.journalkeeper.converter.PartitionGroupReplicaConverter;
import io.chubao.joyqueue.nsr.journalkeeper.repository.PartitionGroupReplicaRepository;
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
    public Replica getById(String id) {
        return PartitionGroupReplicaConverter.convert(partitionGroupReplicaRepository.getById(id));
    }

    @Override
    public List<Replica> getByTopic(TopicName topic) {
        return PartitionGroupReplicaConverter.convert(partitionGroupReplicaRepository.getByTopic(topic.getCode(), topic.getNamespace()));
    }

    @Override
    public List<Replica> getByTopicAndGroup(TopicName topic, int groupNo) {
        return PartitionGroupReplicaConverter.convert(partitionGroupReplicaRepository.getByTopicAndGroup(topic.getCode(), topic.getNamespace(), groupNo));
    }

    @Override
    public List<Replica> getByBrokerId(Integer brokerId) {
        return PartitionGroupReplicaConverter.convert(partitionGroupReplicaRepository.getByBrokerId(Long.valueOf(String.valueOf(brokerId))));
    }

    @Override
    public Replica add(Replica replica) {
        return PartitionGroupReplicaConverter.convert(partitionGroupReplicaRepository.add(PartitionGroupReplicaConverter.convert(replica)));
    }

    @Override
    public Replica update(Replica replica) {
        return PartitionGroupReplicaConverter.convert(partitionGroupReplicaRepository.update(PartitionGroupReplicaConverter.convert(replica)));
    }

    @Override
    public void delete(String id) {
        partitionGroupReplicaRepository.deleteById(id);
    }
}