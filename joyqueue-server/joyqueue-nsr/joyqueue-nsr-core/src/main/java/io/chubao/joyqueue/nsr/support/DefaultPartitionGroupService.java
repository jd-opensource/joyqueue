package io.chubao.joyqueue.nsr.support;

import io.chubao.joyqueue.domain.PartitionGroup;
import io.chubao.joyqueue.domain.TopicName;
import io.chubao.joyqueue.nsr.service.PartitionGroupService;
import io.chubao.joyqueue.nsr.service.internal.PartitionGroupInternalService;

import java.util.List;

/**
 * DefaultPartitionGroupService
 * author: gaohaoxiang
 * date: 2019/8/27
 */
public class DefaultPartitionGroupService implements PartitionGroupService {

    private PartitionGroupInternalService partitionGroupInternalService;

    public DefaultPartitionGroupService(PartitionGroupInternalService partitionGroupInternalService) {
        this.partitionGroupInternalService = partitionGroupInternalService;
    }

    @Override
    public PartitionGroup getById(String id) {
        return partitionGroupInternalService.getById(id);
    }

    @Override
    public PartitionGroup getByTopicAndGroup(TopicName topic, int group) {
        return partitionGroupInternalService.getByTopicAndGroup(topic, group);
    }

    @Override
    public List<PartitionGroup> getByTopic(TopicName topic) {
        return partitionGroupInternalService.getByTopic(topic);
    }

    @Override
    public List<PartitionGroup> getAll() {
        return partitionGroupInternalService.getAll();
    }

    @Override
    public PartitionGroup add(PartitionGroup partitionGroup) {
        return partitionGroupInternalService.add(partitionGroup);
    }

    @Override
    public PartitionGroup update(PartitionGroup partitionGroup) {
        return partitionGroupInternalService.update(partitionGroup);
    }

    @Override
    public void delete(String id) {
        partitionGroupInternalService.delete(id);
    }
}
