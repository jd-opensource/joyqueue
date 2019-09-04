package io.chubao.joyqueue.nsr.journalkeeper.service;

import io.chubao.joyqueue.domain.PartitionGroup;
import io.chubao.joyqueue.domain.TopicName;
import io.chubao.joyqueue.nsr.journalkeeper.converter.PartitionGroupConverter;
import io.chubao.joyqueue.nsr.journalkeeper.repository.PartitionGroupRepository;
import io.chubao.joyqueue.nsr.service.internal.PartitionGroupInternalService;

import java.util.List;

/**
 * JournalkeeperPartitionGroupInternalService
 * author: gaohaoxiang
 * date: 2019/8/16
 */
public class JournalkeeperPartitionGroupInternalService implements PartitionGroupInternalService {

    private PartitionGroupRepository partitionGroupRepository;

    public JournalkeeperPartitionGroupInternalService(PartitionGroupRepository partitionGroupRepository) {
        this.partitionGroupRepository = partitionGroupRepository;
    }

    @Override
    public PartitionGroup getById(String id) {
        return PartitionGroupConverter.convert(partitionGroupRepository.getById(id));
    }

    @Override
    public PartitionGroup getByTopicAndGroup(TopicName topic, int group) {
        return PartitionGroupConverter.convert(partitionGroupRepository.getByTopicAndGroup(topic.getCode(), topic.getNamespace(), group));
    }

    @Override
    public List<PartitionGroup> getByTopic(TopicName topic) {
        return PartitionGroupConverter.convert(partitionGroupRepository.getByTopic(topic.getCode(), topic.getNamespace()));
    }

    @Override
    public List<PartitionGroup> getAll() {
        return PartitionGroupConverter.convert(partitionGroupRepository.getAll());
    }

    @Override
    public PartitionGroup add(PartitionGroup partitionGroup) {
        return PartitionGroupConverter.convert(partitionGroupRepository.add(PartitionGroupConverter.convert(partitionGroup)));
    }

    @Override
    public PartitionGroup update(PartitionGroup partitionGroup) {
        return PartitionGroupConverter.convert(partitionGroupRepository.update(PartitionGroupConverter.convert(partitionGroup)));
    }

    @Override
    public void delete(String id) {
        partitionGroupRepository.deleteById(id);
    }
}